package com.tvtracker.analytics.dto;

import java.time.LocalDate;

public record WatchStreaksResponse(
        long currentStreakDays,
        long longestStreakDays,
        LocalDate currentStreakStartDate,
        LocalDate longestStreakStartDate,
        LocalDate longestStreakEndDate) {}
