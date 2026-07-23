package com.tvtracker.analytics;

import com.tvtracker.analytics.dto.LibraryCompletionResponse;
import com.tvtracker.analytics.dto.LongestToWatchResponse;
import com.tvtracker.analytics.dto.PlanToWatchCountResponse;
import com.tvtracker.analytics.dto.TotalsResponse;
import com.tvtracker.analytics.dto.WatchCountsResponse;
import com.tvtracker.analytics.dto.WatchStreaksResponse;
import com.tvtracker.common.openapi.ErrorResponse;
import com.tvtracker.common.security.CurrentUser;
import com.tvtracker.favorite.dto.FavoriteResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Analytics")
@SecurityRequirement(name = "bearerAuth")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/watch-counts")
    @Operation(
            summary = "Get WATCHED event counts by target type for a resolved time period",
            description = "MONTH and YEAR derive the end date from from; CUSTOM requires an explicit to date.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Watch counts for resolved period"),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid period or date range",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
                responseCode = "401",
                description = "Missing or invalid JWT",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public WatchCountsResponse watchCounts(
            @Parameter(description = "MONTH, YEAR, or CUSTOM", required = true) @RequestParam AnalyticsPeriod period,
            @Parameter(description = "Anchor date (YYYY-MM-DD)", required = true)
                    @RequestParam
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate from,
            @Parameter(description = "Required for CUSTOM period (YYYY-MM-DD)")
                    @RequestParam(required = false)
                    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate to,
            @CurrentUser UUID userId) {
        return analyticsService.getWatchCounts(userId, period, from, to);
    }

    @GetMapping("/longest-to-watch")
    @Operation(summary = "Get shows ranked by elapsed time between first and last watched episode")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Ranked show durations"),
        @ApiResponse(
                responseCode = "401",
                description = "Missing or invalid JWT",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<LongestToWatchResponse> longestToWatch(@CurrentUser UUID userId) {
        return analyticsService.getLongestToWatch(userId);
    }

    @GetMapping("/totals")
    @Operation(summary = "Get all-time WATCHED event counts grouped by target type")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "All-time watch totals"),
        @ApiResponse(
                responseCode = "401",
                description = "Missing or invalid JWT",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public TotalsResponse totals(@CurrentUser UUID userId) {
        return analyticsService.getTotals(userId);
    }

    @GetMapping("/favorites")
    @Operation(summary = "Get the authenticated user's explicitly favorited shows for analytics")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Explicit favorites only"),
        @ApiResponse(
                responseCode = "401",
                description = "Missing or invalid JWT",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<FavoriteResponse> favorites(@CurrentUser UUID userId) {
        return analyticsService.getFavorites(userId);
    }

    @GetMapping("/watch-streaks")
    @Operation(summary = "Get current and longest consecutive-day watch streaks from the event log")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Current and longest streaks"),
        @ApiResponse(
                responseCode = "401",
                description = "Missing or invalid JWT",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public WatchStreaksResponse watchStreaks(@CurrentUser UUID userId) {
        return analyticsService.getWatchStreaks(userId);
    }

    @GetMapping("/library-completion")
    @Operation(summary = "Get per-show and overall episode completion percentages for the library")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Library completion percentages"),
        @ApiResponse(
                responseCode = "401",
                description = "Missing or invalid JWT",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public LibraryCompletionResponse libraryCompletion(@CurrentUser UUID userId) {
        return analyticsService.getLibraryCompletion(userId);
    }

    @GetMapping("/plan-to-watch-count")
    @Operation(summary = "Get the count of library shows flagged as plan to watch")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Plan-to-watch count"),
        @ApiResponse(
                responseCode = "401",
                description = "Missing or invalid JWT",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public PlanToWatchCountResponse planToWatchCount(@CurrentUser UUID userId) {
        return analyticsService.getPlanToWatchCount(userId);
    }
}
