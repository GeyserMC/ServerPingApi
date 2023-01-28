package org.geysermc.serverpingapi;

import br.com.azalim.mcserverping.MCPing;
import br.com.azalim.mcserverping.MCPingOptions;
import com.nukkitx.protocol.bedrock.BedrockClient;
import com.nukkitx.protocol.bedrock.BedrockPong;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@RestController
public class PingController {
    @GetMapping("/ping")
    public PingResponse ping(@RequestParam(value = "hostname", defaultValue = "") String hostname, @RequestParam(value = "port", defaultValue = "19132") int port, HttpServletRequest request) {
        if (hostname.isBlank()) {
            hostname = request.getRemoteAddr();
        }

        if (port <= 0 || port >= 65535) {
            return new PingResponse(false, "Invalid port specified", null, null);
        }

        try {
            QueryData queryData = new QueryData(hostname, InetAddress.getByName(hostname).getHostAddress(), port);
            return pingServer(queryData, true);
        } catch (UnknownHostException e) {
            return new PingResponse(false, e.getMessage(), null, null);
        }
    }

    private PingResponse pingServer(QueryData queryData, boolean firstPing) {
        BedrockClient client = null;
        PingResponse response = null;
        try {
            InetSocketAddress bindAddress = new InetSocketAddress("0.0.0.0", 0);
            client = new BedrockClient(bindAddress);

            client.bind().join();

            InetSocketAddress addressToPing = new InetSocketAddress(queryData.hostname(), queryData.port());
            BedrockPong pong = client.ping(addressToPing, 1500, TimeUnit.MILLISECONDS).get();

            response = new PingResponse(true, "", queryData, new PingData(!firstPing, pong));
            // TODO Gather more server info
        } catch (InterruptedException | ExecutionException e) {
            if (firstPing) {
                tcpPing(queryData.hostname(), queryData.port());
                response = pingServer(queryData, false);
            } else {
                if (e.getCause() instanceof TimeoutException) {
                    response = new PingResponse(false, "Connection timed out", queryData, null);
                } else {
                    response = new PingResponse(false, e.getMessage(), queryData, null);
                }
            }
        } finally {
            if (client != null) {
                client.close();
            }
        }

        return response;
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
