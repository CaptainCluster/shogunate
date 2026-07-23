package com.tvtracker.analytics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tvtracker.analytics.dto.PlanToWatchCountResponse;
import com.tvtracker.analytics.dto.TotalsResponse;
import com.tvtracker.analytics.dto.WatchStreaksResponse;
import com.tvtracker.common.TargetType;
import com.tvtracker.common.exception.ValidationException;
import com.tvtracker.favorite.FavoriteService;
import com.tvtracker.show.LibraryStatus;
import com.tvtracker.show.UserLibraryRepository;
import com.tvtracker.watch.WatchEventRepository;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private WatchEventRepository watchEventRepository;

    @Mock
    private UserLibraryRepository userLibraryRepository;

    @Mock
    private FavoriteService favoriteService;

    @InjectMocks
    private AnalyticsService analyticsService;

    @Test
    void monthPeriodDerivesEndFromStart() {
        var resolved = analyticsService.resolvePeriod(AnalyticsPeriod.MONTH, LocalDate.of(2026, 1, 15), null);

        assertEquals(LocalDate.of(2026, 1, 1), resolved.from());
        assertEquals(LocalDate.of(2026, 1, 31), resolved.to());
    }

    @Test
    void yearPeriodDerivesFullYearFromAnchor() {
        var resolved = analyticsService.resolvePeriod(AnalyticsPeriod.YEAR, LocalDate.of(2026, 6, 1), null);

        assertEquals(LocalDate.of(2026, 1, 1), resolved.from());
        assertEquals(LocalDate.of(2026, 12, 31), resolved.to());
    }

    @Test
    void customPeriodRequiresTo() {
        assertThrows(
                ValidationException.class,
                () -> analyticsService.resolvePeriod(AnalyticsPeriod.CUSTOM, LocalDate.of(2026, 1, 1), null));
    }

    @Test
    void customPeriodRejectsFromAfterTo() {
        assertThrows(
                ValidationException.class,
                () -> analyticsService.resolvePeriod(
                        AnalyticsPeriod.CUSTOM, LocalDate.of(2026, 2, 1), LocalDate.of(2026, 1, 1)));
    }

    @Test
    void totalsMapMissingTargetTypesToZero() {
        UUID userId = UUID.randomUUID();
        when(watchEventRepository.countWatchedByTargetTypeAllTime(userId))
                .thenReturn(List.of(projection(TargetType.EPISODE, 5)));

        TotalsResponse totals = analyticsService.getTotals(userId);

        assertEquals(5, totals.counts().episodes());
        assertEquals(0, totals.counts().seasons());
        assertEquals(0, totals.counts().shows());
    }

    @Test
    void longestStreakAcrossGap() {
        UUID userId = UUID.randomUUID();
        when(watchEventRepository.findDistinctWatchedDates(userId))
                .thenReturn(List.of(
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 1, 2),
                        LocalDate.of(2026, 1, 3),
                        LocalDate.of(2026, 3, 1),
                        LocalDate.of(2026, 3, 2)));

        WatchStreaksResponse streaks = analyticsService.getWatchStreaks(userId);

        assertEquals(3, streaks.longestStreakDays());
        assertEquals(LocalDate.of(2026, 1, 1), streaks.longestStreakStartDate());
        assertEquals(LocalDate.of(2026, 1, 3), streaks.longestStreakEndDate());
    }

    @Test
    void currentStreakZeroWhenLastActivityTooOld() {
        UUID userId = UUID.randomUUID();
        LocalDate stale = LocalDate.now(ZoneOffset.UTC).minusDays(3);
        when(watchEventRepository.findDistinctWatchedDates(userId)).thenReturn(List.of(stale));

        WatchStreaksResponse streaks = analyticsService.getWatchStreaks(userId);

        assertEquals(0, streaks.currentStreakDays());
        assertNull(streaks.currentStreakStartDate());
    }

    @Test
    void currentStreakCountsThroughYesterday() {
        UUID userId = UUID.randomUUID();
        LocalDate yesterday = LocalDate.now(ZoneOffset.UTC).minusDays(1);
        LocalDate dayBefore = yesterday.minusDays(1);
        when(watchEventRepository.findDistinctWatchedDates(userId)).thenReturn(List.of(dayBefore, yesterday));

        WatchStreaksResponse streaks = analyticsService.getWatchStreaks(userId);

        assertEquals(2, streaks.currentStreakDays());
        assertEquals(dayBefore, streaks.currentStreakStartDate());
    }

    @Test
    void libraryCompletionCalculatesOverallPercent() {
        UUID userId = UUID.randomUUID();
        UUID showId = UUID.randomUUID();
        when(watchEventRepository.findLibraryCompletionByShow(userId))
                .thenReturn(List.of(completionProjection(showId, "Show A", 1, 2)));

        var response = analyticsService.getLibraryCompletion(userId);

        assertEquals(1, response.watchedEpisodes());
        assertEquals(2, response.totalEpisodes());
        assertEquals(50.0, response.overallCompletionPercent());
        assertEquals(50.0, response.shows().get(0).completionPercent());
        assertEquals(false, response.shows().get(0).fullyWatched());
    }

    @Test
    void planToWatchCountUsesLibraryStatus() {
        UUID userId = UUID.randomUUID();
        when(userLibraryRepository.countByUserIdAndLibraryStatus(userId, LibraryStatus.PLAN_TO_WATCH))
                .thenReturn(3L);

        PlanToWatchCountResponse response = analyticsService.getPlanToWatchCount(userId);

        assertEquals(3, response.count());
        verify(userLibraryRepository).countByUserIdAndLibraryStatus(userId, LibraryStatus.PLAN_TO_WATCH);
    }

    private TargetTypeCountProjection projection(TargetType targetType, long count) {
        return new TargetTypeCountProjection() {
            @Override
            public TargetType getTargetType() {
                return targetType;
            }

            @Override
            public long getCount() {
                return count;
            }
        };
    }

    private ShowCompletionProjection completionProjection(UUID showId, String title, long watched, long total) {
        return new ShowCompletionProjection() {
            @Override
            public UUID getShowId() {
                return showId;
            }

            @Override
            public String getTitle() {
                return title;
            }

            @Override
            public long getWatchedEpisodes() {
                return watched;
            }

            @Override
            public long getTotalEpisodes() {
                return total;
            }
        };
    }
}
