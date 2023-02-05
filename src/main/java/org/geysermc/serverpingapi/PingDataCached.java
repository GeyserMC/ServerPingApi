package org.geysermc.serverpingapi;

import java.time.Instant;

public record PingDataCached(PingData pingData, Instant cacheTime) {
}
