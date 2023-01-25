package org.geysermc.serverpingapi;

import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Server {
    private final HttpServer server;

    public Server() throws Exception {
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);

        server = HttpServer.create(new InetSocketAddress("0.0.0.0", 8080), 0);
        server.createContext("/", new PingHandler());
        server.setExecutor(threadPoolExecutor);
    }

    public void start() {
        server.start();
        System.out.println("Server started on port 8080");
    }

    public void stop() {
        server.stop(0);
    }
}
