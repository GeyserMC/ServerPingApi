package org.geysermc.serverpingapi.models;

import java.time.Instant;

public interface PingCached {
    boolean success();

    String message();

    PingData pingData();

    Instant cacheTime();
}
