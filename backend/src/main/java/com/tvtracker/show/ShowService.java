package com.tvtracker.show;

import com.tvtracker.common.TargetType;
import com.tvtracker.common.exception.ConflictException;
import com.tvtracker.common.exception.NotFoundException;
import com.tvtracker.common.exception.ValidationException;
import com.tvtracker.review.ReviewRepository;
import com.tvtracker.show.dto.EpisodeResponse;
import com.tvtracker.show.dto.SeasonResponse;
import com.tvtracker.show.dto.ShowDetailResponse;
import com.tvtracker.show.dto.ShowSearchResult;
import com.tvtracker.show.dto.ShowSummaryResponse;
import com.tvtracker.show.tvmaze.TvmazeClient;
import com.tvtracker.show.tvmaze.TvmazeMapper;
import com.tvtracker.show.tvmaze.TvmazeSearchResult;
import com.tvtracker.show.tvmaze.TvmazeShowRef;
import com.tvtracker.watch.UserWatchState;
import com.tvtracker.watch.UserWatchStateRepository;
import com.tvtracker.watch.WatchEventRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ShowService {

    private final TvmazeClient tvmazeClient;
    private final ShowRepository showRepository;
    private final SeasonRepository seasonRepository;
    private final EpisodeRepository episodeRepository;
    private final UserLibraryRepository userLibraryRepository;
    private final UserWatchStateRepository userWatchStateRepository;
    private final WatchEventRepository watchEventRepository;
    private final ReviewRepository reviewRepository;

    public List<ShowSearchResult> search(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        return tvmazeClient.searchShows(query.trim()).stream()
                .map(TvmazeSearchResult::show)
                .map(this::toSearchResult)
                .toList();
    }

    @Transactional
    public ShowDetailResponse addToLibrary(UUID userId, int tvmazeId) {
        if (userLibraryRepository.existsByUserIdAndTvmazeId(userId, tvmazeId)) {
            throw new ConflictException("Show already in library");
        }

        Show show = showRepository.findByTvmazeId(tvmazeId).orElseGet(() -> createCatalogFromTvmaze(tvmazeId));

        Instant now = Instant.now();
        UserLibrary entry = UserLibrary.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .showId(show.getId())
                .libraryStatus(LibraryStatus.NONE)
                .addedAt(now)
                .build();
        userLibraryRepository.save(entry);

        return getShowDetail(userId, show.getId());
    }

    public List<ShowSummaryResponse> listLibrary(UUID userId) {
        return userLibraryRepository.findByUserIdOrderByAddedAtDesc(userId).stream()
                .map(entry -> {
                    Show show = showRepository
                            .findById(entry.getShowId())
                            .orElseThrow(() -> new NotFoundException("Show not found"));
                    return toSummary(show, entry);
                })
                .toList();
    }

    public ShowDetailResponse getShowDetail(UUID userId, UUID showId) {
        UserLibrary entry = userLibraryRepository
                .findByUserIdAndShowId(userId, showId)
                .orElseThrow(() -> new NotFoundException("Show not in library"));

        Show show = showRepository.findById(showId).orElseThrow(() -> new NotFoundException("Show not found"));

        Set<UUID> targetIds = collectHierarchyTargetIds(showId);
        Map<WatchStateKey, UserWatchState> watchStates = loadWatchStates(userId, targetIds);

        List<Season> seasons = seasonRepository.findByShowIdOrderBySeasonNumberAsc(showId);
        List<SeasonResponse> seasonResponses = seasons.stream()
                .map(season -> {
                    List<EpisodeResponse> episodes =
                            episodeRepository.findBySeasonIdOrderByEpisodeNumberAsc(season.getId()).stream()
                                    .map(ep -> toEpisodeResponse(ep, watchStates))
                                    .toList();
                    return toSeasonResponse(season, watchStates, episodes);
                })
                .toList();

        WatchState showState = resolveWatchState(watchStates, TargetType.SHOW, showId);

        return new ShowDetailResponse(
                show.getId(),
                show.getTvmazeId(),
                show.getTitle(),
                show.getOverview(),
                show.getPosterUrl(),
                show.getTvmazeUrl(),
                show.getFirstAirDate(),
                entry.getLibraryStatus(),
                entry.getAddedAt(),
                showState.watched(),
                showState.watchedAt(),
                seasonResponses);
    }

    @Transactional
    public ShowSummaryResponse updateLibraryStatus(UUID userId, UUID showId, LibraryStatus status) {
        if (status == LibraryStatus.WATCHED) {
            throw new ValidationException("WATCHED status cannot be set manually");
        }

        UserLibrary entry = userLibraryRepository
                .findByUserIdAndShowId(userId, showId)
                .orElseThrow(() -> new NotFoundException("Show not in library"));

        if (entry.getLibraryStatus() == LibraryStatus.WATCHED) {
            throw new ValidationException("Cannot change library status while show is fully watched");
        }

        UserLibrary updated = entry.toBuilder().libraryStatus(status).build();
        userLibraryRepository.save(updated);

        Show show = showRepository.findById(showId).orElseThrow(() -> new NotFoundException("Show not found"));
        return toSummary(show, updated);
    }

    @Transactional
    public void removeFromLibrary(UUID userId, UUID showId) {
        UserLibrary entry = userLibraryRepository
                .findByUserIdAndShowId(userId, showId)
                .orElseThrow(() -> new NotFoundException("Show not in library"));

        Set<UUID> targetIds = collectHierarchyTargetIds(showId);
        userWatchStateRepository.deleteByUserIdAndTargetIdIn(userId, targetIds);
        watchEventRepository.deleteByUserIdAndTargetIdIn(userId, targetIds);
        reviewRepository.deleteByUserIdAndTargetIdIn(userId, targetIds);

        userLibraryRepository.delete(entry);

        if (userLibraryRepository.countByShowId(showId) == 0) {
            deleteOrphanCatalog(showId);
        }
    }

    private Show createCatalogFromTvmaze(int tvmazeId) {
        TvmazeShowRef showRef = tvmazeClient.fetchShow(tvmazeId);
        var episodes = tvmazeClient.fetchEpisodes(tvmazeId);
        TvmazeMapper.CatalogSnapshot snapshot = TvmazeMapper.toCatalogSnapshot(showRef, episodes, Instant.now());

        showRepository.save(snapshot.show());
        seasonRepository.saveAll(snapshot.seasons());
        episodeRepository.saveAll(snapshot.episodes());
        return snapshot.show();
    }

    private Set<UUID> collectHierarchyTargetIds(UUID showId) {
        Set<UUID> targetIds = new HashSet<>();
        targetIds.add(showId);

        List<Season> seasons = seasonRepository.findByShowIdOrderBySeasonNumberAsc(showId);
        List<UUID> seasonIds = new ArrayList<>();
        for (Season season : seasons) {
            targetIds.add(season.getId());
            seasonIds.add(season.getId());
        }

        if (!seasonIds.isEmpty()) {
            episodeRepository.findBySeasonIdIn(seasonIds).forEach(ep -> targetIds.add(ep.getId()));
        }

        return targetIds;
    }

    private Map<WatchStateKey, UserWatchState> loadWatchStates(UUID userId, Set<UUID> targetIds) {
        if (targetIds.isEmpty()) {
            return Map.of();
        }
        Map<WatchStateKey, UserWatchState> states = new HashMap<>();
        userWatchStateRepository
                .findByUserIdAndTargetIdIn(userId, targetIds)
                .forEach(state -> states.put(new WatchStateKey(state.getTargetType(), state.getTargetId()), state));
        return states;
    }

    private EpisodeResponse toEpisodeResponse(Episode episode, Map<WatchStateKey, UserWatchState> watchStates) {
        WatchState state = resolveWatchState(watchStates, TargetType.EPISODE, episode.getId());
        return new EpisodeResponse(
                episode.getId(),
                episode.getEpisodeNumber(),
                episode.getTitle(),
                episode.getAirDate(),
                state.watched(),
                state.watchedAt());
    }

    private SeasonResponse toSeasonResponse(
            Season season, Map<WatchStateKey, UserWatchState> watchStates, List<EpisodeResponse> episodes) {
        WatchState state = resolveWatchState(watchStates, TargetType.SEASON, season.getId());
        return new SeasonResponse(
                season.getId(),
                season.getSeasonNumber(),
                season.getName(),
                state.watched(),
                state.watchedAt(),
                episodes);
    }

    private WatchState resolveWatchState(Map<WatchStateKey, UserWatchState> watchStates, TargetType type, UUID id) {
        UserWatchState state = watchStates.get(new WatchStateKey(type, id));
        if (state == null || !state.isWatched()) {
            return new WatchState(false, null);
        }
        return new WatchState(true, state.getWatchedAt());
    }

    private void deleteOrphanCatalog(UUID showId) {
        List<Season> seasons = seasonRepository.findByShowIdOrderBySeasonNumberAsc(showId);
        List<UUID> seasonIds = seasons.stream().map(Season::getId).toList();
        if (!seasonIds.isEmpty()) {
            episodeRepository.deleteBySeasonIdIn(seasonIds);
            seasonRepository.deleteByShowId(showId);
        }
        showRepository.deleteById(showId);
    }

    private ShowSearchResult toSearchResult(TvmazeShowRef ref) {
        return new ShowSearchResult(
                ref.id(),
                ref.name(),
                TvmazeMapper.stripHtml(ref.summary()),
                ref.image() != null ? ref.image().medium() : null,
                ref.url(),
                TvmazeMapper.parseDate(ref.premiered()));
    }

    private ShowSummaryResponse toSummary(Show show, UserLibrary entry) {
        return new ShowSummaryResponse(
                show.getId(),
                show.getTvmazeId(),
                show.getTitle(),
                show.getOverview(),
                show.getPosterUrl(),
                show.getTvmazeUrl(),
                show.getFirstAirDate(),
                entry.getLibraryStatus(),
                entry.getAddedAt());
    }

    private record WatchStateKey(TargetType targetType, UUID targetId) {}

    private record WatchState(boolean watched, Instant watchedAt) {}
}
