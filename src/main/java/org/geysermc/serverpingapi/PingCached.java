package org.geysermc.serverpingapi;

import java.time.Instant;

public interface PingCached {
    boolean success();

    String message();

    PingData pingData();

    Instant cacheTime();
}
