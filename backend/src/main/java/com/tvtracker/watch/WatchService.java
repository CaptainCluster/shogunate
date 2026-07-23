package com.tvtracker.watch;

import com.tvtracker.common.TargetType;
import com.tvtracker.common.exception.ForbiddenException;
import com.tvtracker.common.exception.NotFoundException;
import com.tvtracker.common.exception.ValidationException;
import com.tvtracker.show.Episode;
import com.tvtracker.show.EpisodeRepository;
import com.tvtracker.show.LibraryStatusSyncService;
import com.tvtracker.show.Season;
import com.tvtracker.show.SeasonRepository;
import com.tvtracker.show.ShowRepository;
import com.tvtracker.show.UserLibraryRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WatchService {

    private final ShowRepository showRepository;
    private final SeasonRepository seasonRepository;
    private final EpisodeRepository episodeRepository;
    private final UserLibraryRepository userLibraryRepository;
    private final UserWatchStateRepository userWatchStateRepository;
    private final WatchEventRepository watchEventRepository;
    private final WatchHierarchySyncService watchHierarchySyncService;
    private final LibraryStatusSyncService libraryStatusSyncService;

    @Transactional
    public void markWatched(UUID userId, TargetType targetType, UUID targetId) {
        UUID showId = resolveShowId(targetType, targetId);
        verifyLibraryMembership(userId, showId);

        List<WatchTarget> targets = collectTargets(targetType, targetId, showId);
        Instant now = Instant.now();
        List<WatchTarget> changedTargets = new ArrayList<>();

        for (int i = 0; i < targets.size(); i++) {
            WatchTarget target = targets.get(i);
            boolean isTopLevel = i == 0;
            Optional<UserWatchState> existing = findWatchState(userId, target);

            if (!isTopLevel && existing.map(UserWatchState::isWatched).orElse(false)) {
                continue;
            }

            upsertWatchState(userId, target, true, now);
            changedTargets.add(target);
        }

        UUID topEventId = appendWatchEvents(userId, changedTargets, WatchAction.WATCHED, now);
        watchHierarchySyncService.syncAfterWatchChange(userId, showId, topEventId, now, WatchAction.WATCHED);
        libraryStatusSyncService.syncAfterWatchChange(userId, showId);
    }

    @Transactional
    public void unmarkWatched(UUID userId, TargetType targetType, UUID targetId, boolean confirm) {
        if ((targetType == TargetType.SEASON || targetType == TargetType.SHOW) && !confirm) {
            throw new ValidationException("Confirmation required to unmark a season or show (confirm=true)");
        }

        UUID showId = resolveShowId(targetType, targetId);
        verifyLibraryMembership(userId, showId);

        List<WatchTarget> targets = collectTargets(targetType, targetId, showId);
        Instant now = Instant.now();

        for (WatchTarget target : targets) {
            upsertWatchState(userId, target, false, null);
        }

        UUID topEventId = appendWatchEvents(userId, targets, WatchAction.UNWATCHED, now);
        watchHierarchySyncService.syncAfterWatchChange(userId, showId, topEventId, now, WatchAction.UNWATCHED);
        libraryStatusSyncService.syncAfterWatchChange(userId, showId);
    }

    private Optional<UserWatchState> findWatchState(UUID userId, WatchTarget target) {
        UserWatchState.UserWatchStateId id =
                new UserWatchState.UserWatchStateId(userId, target.targetType(), target.targetId());
        return userWatchStateRepository.findById(id);
    }

    private void upsertWatchState(UUID userId, WatchTarget target, boolean watched, Instant watchedAt) {
        UserWatchState.UserWatchStateId id =
                new UserWatchState.UserWatchStateId(userId, target.targetType(), target.targetId());
        UserWatchState state = userWatchStateRepository
                .findById(id)
                .map(existing -> existing.toBuilder()
                        .watched(watched)
                        .watchedAt(watchedAt)
                        .build())
                .orElseGet(() -> UserWatchState.builder()
                        .userId(userId)
                        .targetType(target.targetType())
                        .targetId(target.targetId())
                        .watched(watched)
                        .watchedAt(watchedAt)
                        .build());
        userWatchStateRepository.save(state);
    }

    private UUID appendWatchEvents(UUID userId, List<WatchTarget> targets, WatchAction action, Instant occurredAt) {
        if (targets.isEmpty()) {
            return null;
        }

        WatchTarget top = targets.getFirst();
        WatchEvent topEvent = WatchEvent.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .targetType(top.targetType())
                .targetId(top.targetId())
                .action(action)
                .occurredAt(occurredAt)
                .triggeredByCascade(false)
                .cascadeSourceId(null)
                .build();
        watchEventRepository.save(topEvent);

        for (int i = 1; i < targets.size(); i++) {
            WatchTarget target = targets.get(i);
            watchEventRepository.save(WatchEvent.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .targetType(target.targetType())
                    .targetId(target.targetId())
                    .action(action)
                    .occurredAt(occurredAt)
                    .triggeredByCascade(true)
                    .cascadeSourceId(topEvent.getId())
                    .build());
        }

        return topEvent.getId();
    }

    private UUID resolveShowId(TargetType targetType, UUID targetId) {
        return switch (targetType) {
            case SHOW -> showRepository
                    .findById(targetId)
                    .orElseThrow(() -> new NotFoundException("Show not found"))
                    .getId();
            case SEASON -> {
                Season season = seasonRepository
                        .findById(targetId)
                        .orElseThrow(() -> new NotFoundException("Season not found"));
                yield season.getShowId();
            }
            case EPISODE -> {
                Episode episode = episodeRepository
                        .findById(targetId)
                        .orElseThrow(() -> new NotFoundException("Episode not found"));
                Season season = seasonRepository
                        .findById(episode.getSeasonId())
                        .orElseThrow(() -> new NotFoundException("Season not found"));
                yield season.getShowId();
            }
        };
    }

    private void verifyLibraryMembership(UUID userId, UUID showId) {
        if (!userLibraryRepository.existsByUserIdAndShowId(userId, showId)) {
            throw new ForbiddenException("Show not in library");
        }
    }

    private List<WatchTarget> collectTargets(TargetType targetType, UUID targetId, UUID showId) {
        List<WatchTarget> targets = new ArrayList<>();

        switch (targetType) {
            case EPISODE -> targets.add(new WatchTarget(TargetType.EPISODE, targetId));
            case SEASON -> {
                targets.add(new WatchTarget(TargetType.SEASON, targetId));
                episodeRepository.findBySeasonIdOrderByEpisodeNumberAsc(targetId).stream()
                        .map(ep -> new WatchTarget(TargetType.EPISODE, ep.getId()))
                        .forEach(targets::add);
            }
            case SHOW -> {
                targets.add(new WatchTarget(TargetType.SHOW, targetId));
                List<Season> seasons = seasonRepository.findByShowIdOrderBySeasonNumberAsc(showId);
                for (Season season : seasons) {
                    targets.add(new WatchTarget(TargetType.SEASON, season.getId()));
                    episodeRepository.findBySeasonIdOrderByEpisodeNumberAsc(season.getId()).stream()
                            .map(ep -> new WatchTarget(TargetType.EPISODE, ep.getId()))
                            .forEach(targets::add);
                }
            }
        }

        return targets;
    }

    private record WatchTarget(TargetType targetType, UUID targetId) {}
}
