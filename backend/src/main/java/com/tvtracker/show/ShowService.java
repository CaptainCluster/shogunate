package com.tvtracker.show;

import com.tvtracker.common.exception.ConflictException;
import com.tvtracker.common.exception.NotFoundException;
import com.tvtracker.show.dto.EpisodeResponse;
import com.tvtracker.show.dto.SeasonResponse;
import com.tvtracker.show.dto.ShowDetailResponse;
import com.tvtracker.show.dto.ShowSearchResult;
import com.tvtracker.show.dto.ShowSummaryResponse;
import com.tvtracker.show.tvmaze.TvmazeClient;
import com.tvtracker.show.tvmaze.TvmazeMapper;
import com.tvtracker.show.tvmaze.TvmazeSearchResult;
import com.tvtracker.show.tvmaze.TvmazeShowRef;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ShowService {

    private final TvmazeClient tvmazeClient;
    private final ShowRepository showRepository;
    private final SeasonRepository seasonRepository;
    private final EpisodeRepository episodeRepository;
    private final UserLibraryRepository userLibraryRepository;
    private final UserWatchStateRepository userWatchStateRepository;

    public ShowService(
            TvmazeClient tvmazeClient,
            ShowRepository showRepository,
            SeasonRepository seasonRepository,
            EpisodeRepository episodeRepository,
            UserLibraryRepository userLibraryRepository,
            UserWatchStateRepository userWatchStateRepository) {
        this.tvmazeClient = tvmazeClient;
        this.showRepository = showRepository;
        this.seasonRepository = seasonRepository;
        this.episodeRepository = episodeRepository;
        this.userLibraryRepository = userLibraryRepository;
        this.userWatchStateRepository = userWatchStateRepository;
    }

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
        UserLibrary entry = new UserLibrary(UUID.randomUUID(), userId, show.getId(), LibraryStatus.NONE, now);
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

        List<Season> seasons = seasonRepository.findByShowIdOrderBySeasonNumberAsc(showId);
        List<SeasonResponse> seasonResponses = seasons.stream()
                .map(season -> {
                    List<EpisodeResponse> episodes =
                            episodeRepository.findBySeasonIdOrderByEpisodeNumberAsc(season.getId()).stream()
                                    .map(ep -> new EpisodeResponse(
                                            ep.getId(), ep.getEpisodeNumber(), ep.getTitle(), ep.getAirDate()))
                                    .toList();
                    return new SeasonResponse(season.getId(), season.getSeasonNumber(), season.getName(), episodes);
                })
                .toList();

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
                seasonResponses);
    }

    @Transactional
    public ShowSummaryResponse updateLibraryStatus(UUID userId, UUID showId, LibraryStatus status) {
        UserLibrary entry = userLibraryRepository
                .findByUserIdAndShowId(userId, showId)
                .orElseThrow(() -> new NotFoundException("Show not in library"));
        entry.setLibraryStatus(status);
        userLibraryRepository.save(entry);

        Show show = showRepository.findById(showId).orElseThrow(() -> new NotFoundException("Show not found"));
        return toSummary(show, entry);
    }

    /**
     * Removes a show from the user's library and cleans up user-scoped data.
     *
     * <p>Future hooks when watch/review/favorite tables exist:
     *
     * <ul>
     *   <li>DELETE FROM reviews WHERE user_id = ? AND target_id IN (hierarchy ids)
     *   <li>DELETE FROM favorites WHERE user_id = ? AND target_id IN (hierarchy ids)
     *   <li>DELETE FROM watch_events WHERE user_id = ? AND target_id IN (hierarchy ids)
     * </ul>
     */
    @Transactional
    public void removeFromLibrary(UUID userId, UUID showId) {
        UserLibrary entry = userLibraryRepository
                .findByUserIdAndShowId(userId, showId)
                .orElseThrow(() -> new NotFoundException("Show not in library"));

        Set<UUID> targetIds = collectHierarchyTargetIds(showId);
        userWatchStateRepository.deleteByUserIdAndTargetIdIn(userId, targetIds);
        // Future: delete reviews, favorites, watch_events for userId + targetIds

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
}
