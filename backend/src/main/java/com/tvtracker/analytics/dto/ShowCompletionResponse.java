package com.tvtracker.analytics.dto;

import java.util.UUID;

public record ShowCompletionResponse(
        UUID showId,
        String title,
        long watchedEpisodes,
        long totalEpisodes,
        double completionPercent,
        boolean fullyWatched) {}
