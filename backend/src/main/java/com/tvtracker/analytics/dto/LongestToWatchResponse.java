package com.tvtracker.analytics.dto;

import java.time.Instant;
import java.util.UUID;

public record LongestToWatchResponse(
        UUID showId, String title, long durationSeconds, Instant firstWatchedAt, Instant lastWatchedAt) {}
