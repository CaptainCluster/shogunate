package com.tvtracker.watch;

import com.tvtracker.common.TargetType;
import com.tvtracker.show.Episode;
import com.tvtracker.show.EpisodeRepository;
import com.tvtracker.show.Season;
import com.tvtracker.show.SeasonRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WatchHierarchySyncService {

    private final SeasonRepository seasonRepository;
    private final EpisodeRepository episodeRepository;
    private final UserWatchStateRepository userWatchStateRepository;
    private final WatchEventRepository watchEventRepository;

    public void syncAfterWatchChange(
            UUID userId, UUID showId, UUID cascadeSourceEventId, Instant occurredAt, WatchAction action) {
        if (action == WatchAction.WATCHED) {
            syncUpwardMark(userId, showId, cascadeSourceEventId, occurredAt);
        } else {
            syncUpwardUnmark(userId, showId, cascadeSourceEventId, occurredAt);
        }
    }

    private void syncUpwardMark(UUID userId, UUID showId, UUID cascadeSourceEventId, Instant occurredAt) {
        List<Season> seasons = seasonRepository.findByShowIdOrderBySeasonNumberAsc(showId);
        if (seasons.isEmpty()) {
            return;
        }

        List<UUID> seasonIds = seasons.stream().map(Season::getId).toList();
        List<Episode> episodes = episodeRepository.findBySeasonIdIn(seasonIds);
        if (episodes.isEmpty()) {
            return;
        }

        Map<UUID, UserWatchState> watchStates = loadWatchStates(userId, episodes, seasons, showId);
        Instant showMaxTimestamp = null;
        boolean allEpisodesWatched = true;

        for (Season season : seasons) {
            List<Episode> seasonEpisodes = episodes.stream()
                    .filter(ep -> ep.getSeasonId().equals(season.getId()))
                    .toList();
            if (seasonEpisodes.isEmpty()) {
                allEpisodesWatched = false;
                continue;
            }

            Optional<Instant> seasonMaxTimestamp = maxWatchedTimestamp(seasonEpisodes, watchStates);
            if (seasonMaxTimestamp.isEmpty()) {
                allEpisodesWatched = false;
                continue;
            }

            promoteIfNeeded(
                    userId,
                    TargetType.SEASON,
                    season.getId(),
                    seasonMaxTimestamp.get(),
                    cascadeSourceEventId,
                    occurredAt,
                    watchStates);

            showMaxTimestamp = latestTimestamp(showMaxTimestamp, seasonMaxTimestamp.get());
        }

        if (allEpisodesWatched && showMaxTimestamp != null) {
            promoteIfNeeded(
                    userId, TargetType.SHOW, showId, showMaxTimestamp, cascadeSourceEventId, occurredAt, watchStates);
        }
    }

    private void syncUpwardUnmark(UUID userId, UUID showId, UUID cascadeSourceEventId, Instant occurredAt) {
        List<Season> seasons = seasonRepository.findByShowIdOrderBySeasonNumberAsc(showId);
        if (seasons.isEmpty()) {
            return;
        }

        List<UUID> seasonIds = seasons.stream().map(Season::getId).toList();
        List<Episode> episodes = episodeRepository.findBySeasonIdIn(seasonIds);
        if (episodes.isEmpty()) {
            return;
        }

        Map<UUID, UserWatchState> watchStates = loadWatchStates(userId, episodes, seasons, showId);

        for (Season season : seasons) {
            List<Episode> seasonEpisodes = episodes.stream()
                    .filter(ep -> ep.getSeasonId().equals(season.getId()))
                    .toList();
            if (seasonEpisodes.isEmpty()) {
                continue;
            }

            if (!allEpisodesWatched(seasonEpisodes, watchStates)) {
                demoteIfNeeded(
                        userId, TargetType.SEASON, season.getId(), cascadeSourceEventId, occurredAt, watchStates);
            }
        }

        if (!allEpisodesWatched(episodes, watchStates)) {
            demoteIfNeeded(userId, TargetType.SHOW, showId, cascadeSourceEventId, occurredAt, watchStates);
        }
    }

    private Map<UUID, UserWatchState> loadWatchStates(
            UUID userId, List<Episode> episodes, List<Season> seasons, UUID showId) {
        List<UUID> targetIds = new ArrayList<>();
        targetIds.add(showId);
        seasons.forEach(season -> targetIds.add(season.getId()));
        episodes.forEach(episode -> targetIds.add(episode.getId()));

        Map<UUID, UserWatchState> watchStates = new HashMap<>();
        userWatchStateRepository
                .findByUserIdAndTargetIdIn(userId, targetIds)
                .forEach(state -> watchStates.put(state.getTargetId(), state));
        return watchStates;
    }

    private Optional<Instant> maxWatchedTimestamp(List<Episode> episodes, Map<UUID, UserWatchState> watchStates) {
        Instant max = null;
        for (Episode episode : episodes) {
            UserWatchState state = watchStates.get(episode.getId());
            if (state == null || !state.isWatched() || state.getWatchedAt() == null) {
                return Optional.empty();
            }
            max = latestTimestamp(max, state.getWatchedAt());
        }
        return Optional.ofNullable(max);
    }

    private boolean allEpisodesWatched(List<Episode> episodes, Map<UUID, UserWatchState> watchStates) {
        return maxWatchedTimestamp(episodes, watchStates).isPresent();
    }

    private void promoteIfNeeded(
            UUID userId,
            TargetType targetType,
            UUID targetId,
            Instant watchedAt,
            UUID cascadeSourceEventId,
            Instant occurredAt,
            Map<UUID, UserWatchState> watchStates) {
        UserWatchState existing = watchStates.get(targetId);
        if (existing != null && existing.isWatched() && Objects.equals(existing.getWatchedAt(), watchedAt)) {
            return;
        }

        UserWatchState updated = upsertWatchState(userId, targetType, targetId, true, watchedAt);
        watchStates.put(targetId, updated);
        appendCascadeEvent(userId, targetType, targetId, WatchAction.WATCHED, occurredAt, cascadeSourceEventId);
    }

    private void demoteIfNeeded(
            UUID userId,
            TargetType targetType,
            UUID targetId,
            UUID cascadeSourceEventId,
            Instant occurredAt,
            Map<UUID, UserWatchState> watchStates) {
        UserWatchState existing = watchStates.get(targetId);
        if (existing == null || !existing.isWatched()) {
            return;
        }

        UserWatchState updated = upsertWatchState(userId, targetType, targetId, false, null);
        watchStates.put(targetId, updated);
        appendCascadeEvent(userId, targetType, targetId, WatchAction.UNWATCHED, occurredAt, cascadeSourceEventId);
    }

    private UserWatchState upsertWatchState(
            UUID userId, TargetType targetType, UUID targetId, boolean watched, Instant watchedAt) {
        UserWatchState.UserWatchStateId id = new UserWatchState.UserWatchStateId(userId, targetType, targetId);
        UserWatchState state = userWatchStateRepository
                .findById(id)
                .map(existing -> existing.toBuilder()
                        .watched(watched)
                        .watchedAt(watchedAt)
                        .build())
                .orElseGet(() -> UserWatchState.builder()
                        .userId(userId)
                        .targetType(targetType)
                        .targetId(targetId)
                        .watched(watched)
                        .watchedAt(watchedAt)
                        .build());
        return userWatchStateRepository.save(state);
    }

    private void appendCascadeEvent(
            UUID userId,
            TargetType targetType,
            UUID targetId,
            WatchAction action,
            Instant occurredAt,
            UUID cascadeSourceEventId) {
        watchEventRepository.save(WatchEvent.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .targetType(targetType)
                .targetId(targetId)
                .action(action)
                .occurredAt(occurredAt)
                .triggeredByCascade(true)
                .cascadeSourceId(cascadeSourceEventId)
                .build());
    }

    private Instant latestTimestamp(Instant current, Instant candidate) {
        if (current == null || candidate.isAfter(current)) {
            return candidate;
        }
        return current;
    }
}
