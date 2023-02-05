package org.geysermc.serverpingapi;

import java.time.Instant;

public record PingDataCached(PingData pingData, Instant cacheTime) implements PingCached {
    @Override
    public boolean success() {
        return true;
    }

    @Override
    public String message() {
        return "";
    }
}
