package org.geysermc.serverpingapi.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nukkitx.protocol.bedrock.BedrockClient;
import com.nukkitx.protocol.bedrock.BedrockPong;
import org.cloudburstmc.netty.channel.nethernet.signaling.ClientInfo;
import org.cloudburstmc.netty.channel.nethernet.signaling.HttpSignalingSettings;
import org.cloudburstmc.netty.channel.nethernet.signaling.NetherNetHTTPClientSignaling;
import org.geysermc.serverpingapi.controllers.PingController;
import org.geysermc.serverpingapi.models.PingCached;
import org.geysermc.serverpingapi.models.PingData;
import org.geysermc.serverpingapi.models.PingDataCached;
import org.geysermc.serverpingapi.models.PingTransport;
import org.geysermc.serverpingapi.models.QueryData;
import org.geysermc.serverpingapi.models.UnsuccessfulPingCached;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class PingService {
    private static final BedrockClient BEDROCK_CLIENT;

    Logger logger = LoggerFactory.getLogger(PingController.class);

    @Autowired
    private ObjectMapper objectMapper;

    static {
        InetSocketAddress bindAddress = new InetSocketAddress("0.0.0.0", 0);
        BEDROCK_CLIENT = new BedrockClient(bindAddress);
        BEDROCK_CLIENT.bind().join();
    }

    @Cacheable("servers")
    public PingCached getPing(QueryData queryData) {
        logger.info("Pinging server " + queryData.hostname() + ":" + queryData.port());
        try {
            return new PingDataCached(getPingInner(queryData, true), Instant.now());
        } catch (Exception e) {
            return new UnsuccessfulPingCached(e.getMessage(), Instant.now());
        }
    }

    private PingData getPingInner(QueryData queryData, boolean firstPing) throws Exception {
        InetSocketAddress addressToPing = new InetSocketAddress(queryData.hostname(), queryData.port());

        if (firstPing) {
            try {
                return netherNetPing(addressToPing);
            } catch (Exception ignored) {
                // Not a NetherNet server, fall back to RakNet
            }
        }

        try {
            BedrockPong pong = BEDROCK_CLIENT.ping(addressToPing, 1500, TimeUnit.MILLISECONDS).get();

            return new PingData(PingTransport.RAKNET, !firstPing, pong);
        } catch (InterruptedException | ExecutionException e) {
            if (firstPing) {
                tcpPing(queryData.hostname(), queryData.port());
                return getPingInner(queryData, false);
            } else {
                if (e.getCause() instanceof TimeoutException) {
                    throw new Exception("Connection timed out");
                } else {
                    throw e;
                }
            }
        }
    }

    private PingData netherNetPing(InetSocketAddress address) throws Exception {
        NetherNetHTTPClientSignaling.Probe probe = NetherNetHTTPClientSignaling.probe(address, HttpSignalingSettings.DEFAULT)
            .orTimeout(1500, TimeUnit.MILLISECONDS)
            .get();

        // Return the status document as the server sent it from /v1/join
        JsonNode pong = objectMapper.readTree(probe.motd().toJson());
        return new PingData(PingTransport.NETHERNET, false, pong);
    }

    private void tcpPing(String hostname, int port) {
        // Send a Java Edition status request, the response is not needed
        // TODO: Check if we can just make a connection so we dont have to do a full ping
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(hostname, port), 1500);
            socket.setSoTimeout(1500);

            ByteArrayOutputStream handshake = new ByteArrayOutputStream();
            DataOutputStream handshakeData = new DataOutputStream(handshake);
            byte[] hostnameBytes = hostname.getBytes(StandardCharsets.UTF_8);
            writeVarInt(handshakeData, 0x00); // Handshake packet id
            writeVarInt(handshakeData, 4); // Protocol version
            writeVarInt(handshakeData, hostnameBytes.length);
            handshakeData.write(hostnameBytes);
            handshakeData.writeShort(port);
            writeVarInt(handshakeData, 1); // Next state: status

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            writeVarInt(out, handshake.size());
            out.write(handshake.toByteArray());
            writeVarInt(out, 1); // Status request packet length
            writeVarInt(out, 0x00); // Status request packet id
            out.flush();

            socket.getInputStream().read();
        } catch (IOException ignored) { }
    }

    private static void writeVarInt(DataOutputStream out, int value) throws IOException {
        while ((value & ~0x7F) != 0) {
            out.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        out.writeByte(value);
    }
}
