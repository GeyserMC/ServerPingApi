package org.geysermc.serverpingapi.models;

public record PingResponse(boolean success, String message, QueryData query, PingData ping, CacheData cache) {
    public PingResponse(boolean success, String message) {
        this(success, message, null);
    }
    public PingResponse(boolean success, String message, QueryData query) {
        this(success, message, null, null, null);
    }
}
