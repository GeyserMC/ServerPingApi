package org.geysermc.serverpingapi.models;

import java.time.Instant;

public record UnsuccessfulPingCached(String message, Instant cacheTime) implements PingCached {
    @Override
    public boolean success() {
        return false;
    }

    @Override
    public PingData pingData() {
        return null;
    }
}
