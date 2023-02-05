package org.geysermc.serverpingapi;

import br.com.azalim.mcserverping.MCPing;
import br.com.azalim.mcserverping.MCPingOptions;
import com.nukkitx.protocol.bedrock.BedrockClient;
import com.nukkitx.protocol.bedrock.BedrockPong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class PingService {
    private static final BedrockClient BEDROCK_CLIENT;

    Logger logger = LoggerFactory.getLogger(PingController.class);

    static {
        InetSocketAddress bindAddress = new InetSocketAddress("0.0.0.0", 0);
        BEDROCK_CLIENT = new BedrockClient(bindAddress);
        BEDROCK_CLIENT.bind().join();
    }

    @Cacheable("servers")
    public PingDataCached getPing(QueryData queryData) throws Exception {
        logger.info("Pinging server " + queryData.hostname() + ":" + queryData.port());
        return new PingDataCached(getPingInner(queryData, true), Instant.now());
    }

    private PingData getPingInner(QueryData queryData, boolean firstPing) throws Exception {
        try {
            InetSocketAddress addressToPing = new InetSocketAddress(queryData.hostname(), queryData.port());
            BedrockPong pong = BEDROCK_CLIENT.ping(addressToPing, 1500, TimeUnit.MILLISECONDS).get();

            return new PingData(!firstPing, pong);
        } catch (InterruptedException | ExecutionException e) {
            if (firstPing) {
                tcpPing(queryData.hostname(), queryData.port());
                return getPingInner(queryData, false);
            } else {
                if (e.getCause() instanceof TimeoutException) {
                    throw new AssertionError("Connection timed out");
                } else {
                    throw e;
                }
            }
        }
    }


    private void tcpPing(String hostname, int port) {
        // TODO: Check if we can just make a connection so we dont have to do a full ping
        MCPingOptions options = MCPingOptions.builder()
            .hostname(hostname)
            .port(port)
            .timeout(1500)
            .build();

        try {
            MCPing.getPing(options);
        } catch (IOException ignored) { }
    }
}
