package com.tvtracker.analytics.dto;

import java.util.List;

public record LibraryCompletionResponse(
        double overallCompletionPercent,
        long watchedEpisodes,
        long totalEpisodes,
        List<ShowCompletionResponse> shows) {}
