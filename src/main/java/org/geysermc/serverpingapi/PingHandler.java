package org.geysermc.serverpingapi;

import br.com.azalim.mcserverping.MCPing;
import br.com.azalim.mcserverping.MCPingOptions;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nukkitx.protocol.bedrock.BedrockClient;
import com.nukkitx.protocol.bedrock.BedrockPong;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class PingHandler implements HttpHandler {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    int code;
    PingResponse response;

    int port;
    String hostname;

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        code = 400;
        if (!"GET".equals(exchange.getRequestMethod())) {
            response = new PingResponse(false, "Endpoint only allows GET requests", null);
            respond(exchange);
            return;
        }

//        hostname = "test.geysermc.org";
        hostname = exchange.getRemoteAddress().getHostString();

        try {
            port = Integer.parseInt(exchange.getRequestURI().toString().substring(1));
        } catch (NumberFormatException ignored) {
            response = new PingResponse(false, "Invalid port specified", null);
            respond(exchange);
            return;
        }

        code = 200;

        pingServer(true);

        respond(exchange);
    }

    private void pingServer(boolean firstPing) {
        BedrockClient client = null;
        try {
            InetSocketAddress bindAddress = new InetSocketAddress("0.0.0.0", 0);
            client = new BedrockClient(bindAddress);

            client.bind().join();

            InetSocketAddress addressToPing = new InetSocketAddress(hostname, port);
            BedrockPong pong = client.ping(addressToPing, 1500, TimeUnit.MILLISECONDS).get();

            response = new PingResponse(true, "", new PingData(!firstPing, pong));
        } catch (InterruptedException | ExecutionException e) {
            if (firstPing) {
                tcpPing();
                pingServer(false);
            } else {
                code = 500;
                if (e.getCause() instanceof TimeoutException) {
                    response = new PingResponse(false, "Connection timed out", null);
                } else {
                    response = new PingResponse(false, e.getMessage(), null);
                }
            }
        } finally {
            if (client != null) {
                client.close();
            }
        }
    }

    private void tcpPing() {
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

    private void respond(HttpExchange exchange) throws IOException {
        OutputStream outputStream = exchange.getResponseBody();

        String htmlResponse = OBJECT_MAPPER.writeValueAsString(response);

        exchange.getResponseHeaders().add("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(code, htmlResponse.length());

        outputStream.write(htmlResponse.getBytes());
        outputStream.flush();
        outputStream.close();
    }
}
