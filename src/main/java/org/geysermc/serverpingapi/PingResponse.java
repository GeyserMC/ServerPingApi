package org.geysermc.serverpingapi;

public record PingResponse(boolean success, String message, PingData data) {
}
