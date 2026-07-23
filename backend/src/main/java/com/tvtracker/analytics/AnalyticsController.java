package com.tvtracker.analytics;

import com.tvtracker.analytics.dto.LibraryCompletionResponse;
import com.tvtracker.analytics.dto.LongestToWatchResponse;
import com.tvtracker.analytics.dto.PlanToWatchCountResponse;
import com.tvtracker.analytics.dto.TotalsResponse;
import com.tvtracker.analytics.dto.WatchCountsResponse;
import com.tvtracker.analytics.dto.WatchStreaksResponse;
import com.tvtracker.common.security.CurrentUser;
import com.tvtracker.favorite.dto.FavoriteResponse;
import io.swagger.v3.oas.annotations.Operation;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/watch-counts")
    @Operation(summary = "Get WATCHED event counts by target type for a resolved time period")
    public WatchCountsResponse watchCounts(
            @RequestParam AnalyticsPeriod period,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @CurrentUser UUID userId) {
        return analyticsService.getWatchCounts(userId, period, from, to);
    }

    @GetMapping("/longest-to-watch")
    @Operation(summary = "Get shows ranked by elapsed time between first and last watched episode")
    public List<LongestToWatchResponse> longestToWatch(@CurrentUser UUID userId) {
        return analyticsService.getLongestToWatch(userId);
    }

    @GetMapping("/totals")
    @Operation(summary = "Get all-time WATCHED event counts grouped by target type")
    public TotalsResponse totals(@CurrentUser UUID userId) {
        return analyticsService.getTotals(userId);
    }

    @GetMapping("/favorites")
    @Operation(summary = "Get the authenticated user's explicitly favorited shows for analytics")
    public List<FavoriteResponse> favorites(@CurrentUser UUID userId) {
        return analyticsService.getFavorites(userId);
    }

    @GetMapping("/watch-streaks")
    @Operation(summary = "Get current and longest consecutive-day watch streaks from the event log")
    public WatchStreaksResponse watchStreaks(@CurrentUser UUID userId) {
        return analyticsService.getWatchStreaks(userId);
    }

    @GetMapping("/library-completion")
    @Operation(summary = "Get per-show and overall episode completion percentages for the library")
    public LibraryCompletionResponse libraryCompletion(@CurrentUser UUID userId) {
        return analyticsService.getLibraryCompletion(userId);
    }

    @GetMapping("/plan-to-watch-count")
    @Operation(summary = "Get the count of library shows flagged as plan to watch")
    public PlanToWatchCountResponse planToWatchCount(@CurrentUser UUID userId) {
        return analyticsService.getPlanToWatchCount(userId);
    }
}
