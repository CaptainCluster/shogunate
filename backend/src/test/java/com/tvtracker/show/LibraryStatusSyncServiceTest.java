package com.tvtracker.show;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tvtracker.common.TargetType;
import com.tvtracker.watch.UserWatchState;
import com.tvtracker.watch.UserWatchStateRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LibraryStatusSyncServiceTest {

    @Mock
    private UserLibraryRepository userLibraryRepository;

    @Mock
    private SeasonRepository seasonRepository;

    @Mock
    private EpisodeRepository episodeRepository;

    @Mock
    private UserWatchStateRepository userWatchStateRepository;

    @InjectMocks
    private LibraryStatusSyncService libraryStatusSyncService;

    @Test
    void syncSetsWatchedWhenAllEpisodesWatched() {
        UUID userId = UUID.randomUUID();
        UUID showId = UUID.randomUUID();
        UUID seasonId = UUID.randomUUID();
        UUID episodeId = UUID.randomUUID();
        UserLibrary entry = libraryEntry(userId, showId, LibraryStatus.NONE);

        when(userLibraryRepository.findByUserIdAndShowId(userId, showId)).thenReturn(Optional.of(entry));
        when(seasonRepository.findByShowIdOrderBySeasonNumberAsc(showId)).thenReturn(List.of(season(seasonId, showId)));
        when(episodeRepository.findBySeasonIdIn(List.of(seasonId))).thenReturn(List.of(episode(episodeId, seasonId)));
        when(userWatchStateRepository.findByUserIdAndTargetIdIn(userId, List.of(episodeId)))
                .thenReturn(List.of(watchedEpisode(userId, episodeId)));
        when(userLibraryRepository.save(any(UserLibrary.class))).thenAnswer(invocation -> invocation.getArgument(0));

        libraryStatusSyncService.syncAfterWatchChange(userId, showId);

        ArgumentCaptor<UserLibrary> captor = ArgumentCaptor.forClass(UserLibrary.class);
        verify(userLibraryRepository).save(captor.capture());
        assertEquals(LibraryStatus.WATCHED, captor.getValue().getLibraryStatus());
    }

    @Test
    void syncRevertsToNoneWhenLeavingWatched() {
        UUID userId = UUID.randomUUID();
        UUID showId = UUID.randomUUID();
        UUID seasonId = UUID.randomUUID();
        UUID episodeId = UUID.randomUUID();
        UserLibrary entry = UserLibrary.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .showId(showId)
                .libraryStatus(LibraryStatus.WATCHED)
                .addedAt(Instant.now())
                .build();

        when(userLibraryRepository.findByUserIdAndShowId(userId, showId)).thenReturn(Optional.of(entry));
        when(seasonRepository.findByShowIdOrderBySeasonNumberAsc(showId)).thenReturn(List.of(season(seasonId, showId)));
        when(episodeRepository.findBySeasonIdIn(List.of(seasonId))).thenReturn(List.of(episode(episodeId, seasonId)));
        when(userWatchStateRepository.findByUserIdAndTargetIdIn(userId, List.of(episodeId)))
                .thenReturn(List.of());
        when(userLibraryRepository.save(any(UserLibrary.class))).thenAnswer(invocation -> invocation.getArgument(0));

        libraryStatusSyncService.syncAfterWatchChange(userId, showId);

        ArgumentCaptor<UserLibrary> captor = ArgumentCaptor.forClass(UserLibrary.class);
        verify(userLibraryRepository).save(captor.capture());
        assertEquals(LibraryStatus.NONE, captor.getValue().getLibraryStatus());
    }

    @Test
    void syncDoesNotSetWatchedWhenShowHasZeroEpisodes() {
        UUID userId = UUID.randomUUID();
        UUID showId = UUID.randomUUID();
        UserLibrary entry = libraryEntry(userId, showId, LibraryStatus.NONE);

        when(userLibraryRepository.findByUserIdAndShowId(userId, showId)).thenReturn(Optional.of(entry));
        when(seasonRepository.findByShowIdOrderBySeasonNumberAsc(showId)).thenReturn(List.of());

        libraryStatusSyncService.syncAfterWatchChange(userId, showId);

        verify(userLibraryRepository, never()).save(any(UserLibrary.class));
    }

    private UserLibrary libraryEntry(UUID userId, UUID showId, LibraryStatus status) {
        return UserLibrary.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .showId(showId)
                .libraryStatus(status)
                .addedAt(Instant.now())
                .build();
    }

    private Season season(UUID seasonId, UUID showId) {
        return Season.builder().id(seasonId).showId(showId).seasonNumber(1).build();
    }

    private Episode episode(UUID episodeId, UUID seasonId) {
        return Episode.builder()
                .id(episodeId)
                .seasonId(seasonId)
                .episodeNumber(1)
                .build();
    }

    private UserWatchState watchedEpisode(UUID userId, UUID episodeId) {
        return UserWatchState.builder()
                .userId(userId)
                .targetType(TargetType.EPISODE)
                .targetId(episodeId)
                .watched(true)
                .watchedAt(Instant.now())
                .build();
    }
}
