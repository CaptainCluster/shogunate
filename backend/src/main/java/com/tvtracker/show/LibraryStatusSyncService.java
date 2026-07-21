package com.tvtracker.show;

import com.tvtracker.common.TargetType;
import com.tvtracker.watch.UserWatchState;
import com.tvtracker.watch.UserWatchStateRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LibraryStatusSyncService {

    private final UserLibraryRepository userLibraryRepository;
    private final SeasonRepository seasonRepository;
    private final EpisodeRepository episodeRepository;
    private final UserWatchStateRepository userWatchStateRepository;

    public void syncAfterWatchChange(UUID userId, UUID showId) {
        userLibraryRepository.findByUserIdAndShowId(userId, showId).ifPresent(entry -> {
            boolean fullyWatched = isFullyWatched(userId, showId);

            if (fullyWatched && entry.getLibraryStatus() != LibraryStatus.WATCHED) {
                userLibraryRepository.save(
                        entry.toBuilder().libraryStatus(LibraryStatus.WATCHED).build());
            } else if (!fullyWatched && entry.getLibraryStatus() == LibraryStatus.WATCHED) {
                userLibraryRepository.save(
                        entry.toBuilder().libraryStatus(LibraryStatus.NONE).build());
            }
        });
    }

    private boolean isFullyWatched(UUID userId, UUID showId) {
        List<Season> seasons = seasonRepository.findByShowIdOrderBySeasonNumberAsc(showId);
        if (seasons.isEmpty()) {
            return false;
        }

        List<UUID> seasonIds = seasons.stream().map(Season::getId).toList();
        List<Episode> episodes = episodeRepository.findBySeasonIdIn(seasonIds);
        if (episodes.isEmpty()) {
            return false;
        }

        List<UUID> episodeIds = episodes.stream().map(Episode::getId).toList();
        Map<UUID, UserWatchState> watchStates =
                userWatchStateRepository.findByUserIdAndTargetIdIn(userId, episodeIds).stream()
                        .filter(state -> state.getTargetType() == TargetType.EPISODE)
                        .collect(Collectors.toMap(UserWatchState::getTargetId, Function.identity()));

        for (Episode episode : episodes) {
            UserWatchState state = watchStates.get(episode.getId());
            if (state == null || !state.isWatched()) {
                return false;
            }
        }

        return true;
    }
}
