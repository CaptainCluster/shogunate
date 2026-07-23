package com.tvtracker.analytics;

import java.time.Instant;
import java.util.UUID;

public interface LongestToWatchProjection {

    UUID getShowId();

    String getTitle();

    long getDurationSeconds();

    Instant getFirstWatchedAt();

    Instant getLastWatchedAt();
}
