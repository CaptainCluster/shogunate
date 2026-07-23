package com.tvtracker.analytics;

import com.tvtracker.analytics.dto.LibraryCompletionResponse;
import com.tvtracker.analytics.dto.LongestToWatchResponse;
import com.tvtracker.analytics.dto.PlanToWatchCountResponse;
import com.tvtracker.analytics.dto.ShowCompletionResponse;
import com.tvtracker.analytics.dto.TargetTypeCounts;
import com.tvtracker.analytics.dto.TotalsResponse;
import com.tvtracker.analytics.dto.WatchCountsResponse;
import com.tvtracker.analytics.dto.WatchStreaksResponse;
import com.tvtracker.common.exception.ValidationException;
import com.tvtracker.favorite.FavoriteService;
import com.tvtracker.favorite.dto.FavoriteResponse;
import com.tvtracker.show.LibraryStatus;
import com.tvtracker.show.UserLibraryRepository;
import com.tvtracker.watch.WatchEventRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final WatchEventRepository watchEventRepository;
    private final UserLibraryRepository userLibraryRepository;
    private final FavoriteService favoriteService;

    @Transactional(readOnly = true)
    public WatchCountsResponse getWatchCounts(UUID userId, AnalyticsPeriod period, LocalDate from, LocalDate to) {
        ResolvedPeriod resolved = resolvePeriod(period, from, to);
        TargetTypeCounts counts = loadTargetTypeCounts(
                watchEventRepository.countWatchedByTargetType(userId, resolved.fromInstant(), resolved.toInstant()));
        return new WatchCountsResponse(resolved.period(), resolved.from(), resolved.to(), counts);
    }

    @Transactional(readOnly = true)
    public List<LongestToWatchResponse> getLongestToWatch(UUID userId) {
        return watchEventRepository.findLongestToWatchByShow(userId).stream()
                .map(row -> new LongestToWatchResponse(
                        row.getShowId(),
                        row.getTitle(),
                        row.getDurationSeconds(),
                        row.getFirstWatchedAt(),
                        row.getLastWatchedAt()))
                .toList();
    }

    @Transactional(readOnly = true)
    public TotalsResponse getTotals(UUID userId) {
        TargetTypeCounts counts = loadTargetTypeCounts(watchEventRepository.countWatchedByTargetTypeAllTime(userId));
        return new TotalsResponse(counts);
    }

    @Transactional(readOnly = true)
    public List<FavoriteResponse> getFavorites(UUID userId) {
        return favoriteService.listFavorites(userId);
    }

    @Transactional(readOnly = true)
    public WatchStreaksResponse getWatchStreaks(UUID userId) {
        List<LocalDate> activityDates = watchEventRepository.findDistinctWatchedDates(userId);
        if (activityDates.isEmpty()) {
            return new WatchStreaksResponse(0, 0, null, null, null);
        }

        StreakSummary longest = computeLongestStreak(activityDates);
        StreakSummary current = computeCurrentStreak(activityDates);

        return new WatchStreaksResponse(
                current.days(), longest.days(), current.startDate(), longest.startDate(), longest.endDate());
    }

    @Transactional(readOnly = true)
    public LibraryCompletionResponse getLibraryCompletion(UUID userId) {
        List<ShowCompletionResponse> shows = watchEventRepository.findLibraryCompletionByShow(userId).stream()
                .map(this::toShowCompletion)
                .toList();

        long watchedEpisodes = shows.stream()
                .mapToLong(ShowCompletionResponse::watchedEpisodes)
                .sum();
        long totalEpisodes =
                shows.stream().mapToLong(ShowCompletionResponse::totalEpisodes).sum();
        double overallPercent = totalEpisodes == 0 ? 0.0 : (watchedEpisodes * 100.0) / totalEpisodes;

        return new LibraryCompletionResponse(overallPercent, watchedEpisodes, totalEpisodes, shows);
    }

    @Transactional(readOnly = true)
    public PlanToWatchCountResponse getPlanToWatchCount(UUID userId) {
        long count = userLibraryRepository.countByUserIdAndLibraryStatus(userId, LibraryStatus.PLAN_TO_WATCH);
        return new PlanToWatchCountResponse(count);
    }

    ResolvedPeriod resolvePeriod(AnalyticsPeriod period, LocalDate from, LocalDate to) {
        if (period == null) {
            throw new ValidationException("period is required");
        }
        if (from == null) {
            throw new ValidationException("from is required");
        }

        LocalDate resolvedFrom;
        LocalDate resolvedTo;

        if (period == AnalyticsPeriod.MONTH) {
            resolvedFrom = from.with(TemporalAdjusters.firstDayOfMonth());
            resolvedTo = from.with(TemporalAdjusters.lastDayOfMonth());
        } else if (period == AnalyticsPeriod.YEAR) {
            resolvedFrom = from.with(TemporalAdjusters.firstDayOfYear());
            resolvedTo = from.with(TemporalAdjusters.lastDayOfYear());
        } else if (period == AnalyticsPeriod.CUSTOM) {
            if (to == null) {
                throw new ValidationException("to is required for CUSTOM period");
            }
            if (from.isAfter(to)) {
                throw new ValidationException("from must not be after to");
            }
            resolvedFrom = from;
            resolvedTo = to;
        } else {
            throw new ValidationException("Invalid period: " + period);
        }

        Instant fromInstant = resolvedFrom.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant toInstant = resolvedTo
                .atTime(23, 59, 59, 999_999_999)
                .atZone(ZoneOffset.UTC)
                .toInstant();

        return new ResolvedPeriod(period, resolvedFrom, resolvedTo, fromInstant, toInstant);
    }

    private TargetTypeCounts loadTargetTypeCounts(List<TargetTypeCountProjection> rows) {
        long episodes = 0;
        long seasons = 0;
        long shows = 0;

        for (TargetTypeCountProjection row : rows) {
            switch (row.getTargetType()) {
                case EPISODE -> episodes = row.getCount();
                case SEASON -> seasons = row.getCount();
                case SHOW -> shows = row.getCount();
            }
        }

        return new TargetTypeCounts(episodes, seasons, shows);
    }

    private ShowCompletionResponse toShowCompletion(ShowCompletionProjection row) {
        long total = row.getTotalEpisodes();
        long watched = row.getWatchedEpisodes();
        double percent = total == 0 ? 0.0 : (watched * 100.0) / total;
        return new ShowCompletionResponse(
                row.getShowId(), row.getTitle(), watched, total, percent, watched == total && total > 0);
    }

    private StreakSummary computeLongestStreak(List<LocalDate> dates) {
        long longestDays = 1;
        LocalDate longestStart = dates.getFirst();
        LocalDate longestEnd = dates.getFirst();

        long currentDays = 1;
        LocalDate currentStart = dates.getFirst();

        for (int i = 1; i < dates.size(); i++) {
            if (dates.get(i - 1).plusDays(1).equals(dates.get(i))) {
                currentDays++;
            } else {
                if (currentDays > longestDays) {
                    longestDays = currentDays;
                    longestStart = currentStart;
                    longestEnd = dates.get(i - 1);
                }
                currentDays = 1;
                currentStart = dates.get(i);
            }
        }

        if (currentDays > longestDays) {
            longestDays = currentDays;
            longestStart = currentStart;
            longestEnd = dates.getLast();
        }

        return new StreakSummary(longestDays, longestStart, longestEnd);
    }

    private StreakSummary computeCurrentStreak(List<LocalDate> dates) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate yesterday = today.minusDays(1);
        LocalDate mostRecent = dates.getLast();

        if (!mostRecent.equals(today) && !mostRecent.equals(yesterday)) {
            return new StreakSummary(0, null, null);
        }

        long streakDays = 1;
        LocalDate streakStart = mostRecent;

        for (int i = dates.size() - 2; i >= 0; i--) {
            if (dates.get(i).plusDays(1).equals(dates.get(i + 1))) {
                streakDays++;
                streakStart = dates.get(i);
            } else {
                break;
            }
        }

        return new StreakSummary(streakDays, streakStart, mostRecent);
    }

    record ResolvedPeriod(
            AnalyticsPeriod period, LocalDate from, LocalDate to, Instant fromInstant, Instant toInstant) {}

    private record StreakSummary(long days, LocalDate startDate, LocalDate endDate) {}
}
