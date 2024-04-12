package org.geysermc.serverpingapi.models;

import java.time.Instant;

public record CacheData (Instant cacheTime, long secondsSince, boolean fromCache) {
    public CacheData(Instant cacheTime) {
        this(cacheTime, Instant.now().getEpochSecond() - cacheTime.getEpochSecond());
    }

    public CacheData(Instant cacheTime, long secondsSince) {
        this(cacheTime, secondsSince, secondsSince != 0);
    }
}
