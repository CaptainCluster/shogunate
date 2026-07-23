package com.tvtracker.analytics.dto;

import com.tvtracker.analytics.AnalyticsPeriod;
import java.time.LocalDate;

public record WatchCountsResponse(AnalyticsPeriod period, LocalDate from, LocalDate to, TargetTypeCounts counts) {}
