package com.tvtracker.watch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tvtracker.common.TargetType;
import com.tvtracker.common.exception.ForbiddenException;
import com.tvtracker.common.exception.ValidationException;
import com.tvtracker.show.Episode;
import com.tvtracker.show.EpisodeRepository;
import com.tvtracker.show.Season;
import com.tvtracker.show.SeasonRepository;
import com.tvtracker.show.Show;
import com.tvtracker.show.ShowRepository;
import com.tvtracker.show.UserLibraryRepository;
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
class WatchServiceTest {

    @Mock
    private ShowRepository showRepository;

    @Mock
    private SeasonRepository seasonRepository;

    @Mock
    private EpisodeRepository episodeRepository;

    @Mock
    private UserLibraryRepository userLibraryRepository;

    @Mock
    private UserWatchStateRepository userWatchStateRepository;

    @Mock
    private WatchEventRepository watchEventRepository;

    @InjectMocks
    private WatchService watchService;

    @Test
    void markEpisodeWatchedPersistsStateAndEvent() {
        UUID userId = UUID.randomUUID();
        UUID showId = UUID.randomUUID();
        UUID seasonId = UUID.randomUUID();
        UUID episodeId = UUID.randomUUID();

        stubEpisodeHierarchy(showId, seasonId, episodeId);
        when(userLibraryRepository.existsByUserIdAndShowId(userId, showId)).thenReturn(true);
        when(userWatchStateRepository.findById(any())).thenReturn(Optional.empty());
        when(userWatchStateRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(watchEventRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        watchService.markWatched(userId, TargetType.EPISODE, episodeId);

        ArgumentCaptor<UserWatchState> stateCaptor = ArgumentCaptor.forClass(UserWatchState.class);
        verify(userWatchStateRepository).save(stateCaptor.capture());
        assertEquals(episodeId, stateCaptor.getValue().getTargetId());
        assertEquals(true, stateCaptor.getValue().isWatched());

        ArgumentCaptor<WatchEvent> eventCaptor = ArgumentCaptor.forClass(WatchEvent.class);
        verify(watchEventRepository).save(eventCaptor.capture());
        assertEquals(WatchAction.WATCHED, eventCaptor.getValue().getAction());
        assertEquals(false, eventCaptor.getValue().isTriggeredByCascade());
    }

    @Test
    void markShowWatchedCascadesWithSharedTimestampAndEventFlags() {
        UUID userId = UUID.randomUUID();
        UUID showId = UUID.randomUUID();
        UUID seasonId = UUID.randomUUID();
        UUID episodeId = UUID.randomUUID();

        when(showRepository.findById(showId)).thenReturn(Optional.of(show(showId)));
        when(userLibraryRepository.existsByUserIdAndShowId(userId, showId)).thenReturn(true);
        when(seasonRepository.findByShowIdOrderBySeasonNumberAsc(showId)).thenReturn(List.of(season(showId, seasonId)));
        when(episodeRepository.findBySeasonIdOrderByEpisodeNumberAsc(seasonId))
                .thenReturn(List.of(episode(seasonId, episodeId)));
        when(userWatchStateRepository.findById(any())).thenReturn(Optional.empty());
        when(userWatchStateRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(watchEventRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        watchService.markWatched(userId, TargetType.SHOW, showId);

        verify(userWatchStateRepository, org.mockito.Mockito.times(3)).save(any());
        ArgumentCaptor<WatchEvent> eventCaptor = ArgumentCaptor.forClass(WatchEvent.class);
        verify(watchEventRepository, org.mockito.Mockito.times(3)).save(eventCaptor.capture());

        List<WatchEvent> events = eventCaptor.getAllValues();
        assertEquals(TargetType.SHOW, events.getFirst().getTargetType());
        assertEquals(false, events.getFirst().isTriggeredByCascade());

        UUID cascadeSourceId = events.getFirst().getId();
        assertEquals(true, events.get(1).isTriggeredByCascade());
        assertEquals(cascadeSourceId, events.get(1).getCascadeSourceId());
        assertEquals(true, events.get(2).isTriggeredByCascade());
        assertEquals(cascadeSourceId, events.get(2).getCascadeSourceId());

        ArgumentCaptor<UserWatchState> stateCaptor = ArgumentCaptor.forClass(UserWatchState.class);
        verify(userWatchStateRepository, org.mockito.Mockito.times(3)).save(stateCaptor.capture());
        Instant firstTimestamp = stateCaptor.getAllValues().getFirst().getWatchedAt();
        assertEquals(firstTimestamp, stateCaptor.getAllValues().get(1).getWatchedAt());
        assertEquals(firstTimestamp, stateCaptor.getAllValues().get(2).getWatchedAt());
    }

    @Test
    void remMarkUpdatesExistingTimestamp() {
        UUID userId = UUID.randomUUID();
        UUID showId = UUID.randomUUID();
        UUID seasonId = UUID.randomUUID();
        UUID episodeId = UUID.randomUUID();
        Instant previous = Instant.parse("2020-01-01T00:00:00Z");

        stubEpisodeHierarchy(showId, seasonId, episodeId);
        when(userLibraryRepository.existsByUserIdAndShowId(userId, showId)).thenReturn(true);
        when(userWatchStateRepository.findById(any()))
                .thenReturn(Optional.of(UserWatchState.builder()
                        .userId(userId)
                        .targetType(TargetType.EPISODE)
                        .targetId(episodeId)
                        .watched(true)
                        .watchedAt(previous)
                        .build()));
        when(userWatchStateRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(watchEventRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        watchService.markWatched(userId, TargetType.EPISODE, episodeId);

        ArgumentCaptor<UserWatchState> stateCaptor = ArgumentCaptor.forClass(UserWatchState.class);
        verify(userWatchStateRepository).save(stateCaptor.capture());
        org.junit.jupiter.api.Assertions.assertNotEquals(
                previous, stateCaptor.getValue().getWatchedAt());
    }

    @Test
    void unmarkShowWithoutConfirmThrows() {
        UUID userId = UUID.randomUUID();
        UUID showId = UUID.randomUUID();

        assertThrows(
                ValidationException.class, () -> watchService.unmarkWatched(userId, TargetType.SHOW, showId, false));

        verify(userWatchStateRepository, never()).save(any());
        verify(watchEventRepository, never()).save(any());
    }

    @Test
    void unmarkShowWithConfirmClearsHierarchy() {
        UUID userId = UUID.randomUUID();
        UUID showId = UUID.randomUUID();
        UUID seasonId = UUID.randomUUID();
        UUID episodeId = UUID.randomUUID();

        when(showRepository.findById(showId)).thenReturn(Optional.of(show(showId)));
        when(userLibraryRepository.existsByUserIdAndShowId(userId, showId)).thenReturn(true);
        when(seasonRepository.findByShowIdOrderBySeasonNumberAsc(showId)).thenReturn(List.of(season(showId, seasonId)));
        when(episodeRepository.findBySeasonIdOrderByEpisodeNumberAsc(seasonId))
                .thenReturn(List.of(episode(seasonId, episodeId)));
        when(userWatchStateRepository.findById(any()))
                .thenReturn(Optional.of(UserWatchState.builder()
                        .userId(userId)
                        .targetType(TargetType.SHOW)
                        .targetId(showId)
                        .watched(true)
                        .watchedAt(Instant.now())
                        .build()));
        when(userWatchStateRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(watchEventRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        watchService.unmarkWatched(userId, TargetType.SHOW, showId, true);

        ArgumentCaptor<UserWatchState> stateCaptor = ArgumentCaptor.forClass(UserWatchState.class);
        verify(userWatchStateRepository, org.mockito.Mockito.times(3)).save(stateCaptor.capture());
        assertEquals(false, stateCaptor.getValue().isWatched());
        assertEquals(null, stateCaptor.getValue().getWatchedAt());

        ArgumentCaptor<WatchEvent> eventCaptor = ArgumentCaptor.forClass(WatchEvent.class);
        verify(watchEventRepository, org.mockito.Mockito.times(3)).save(eventCaptor.capture());
        assertEquals(
                WatchAction.UNWATCHED, eventCaptor.getAllValues().getFirst().getAction());
    }

    @Test
    void markWatchedRejectsWhenShowNotInLibrary() {
        UUID userId = UUID.randomUUID();
        UUID showId = UUID.randomUUID();
        UUID episodeId = UUID.randomUUID();
        UUID seasonId = UUID.randomUUID();

        stubEpisodeHierarchy(showId, seasonId, episodeId);
        when(userLibraryRepository.existsByUserIdAndShowId(userId, showId)).thenReturn(false);

        assertThrows(ForbiddenException.class, () -> watchService.markWatched(userId, TargetType.EPISODE, episodeId));
    }

    private void stubEpisodeHierarchy(UUID showId, UUID seasonId, UUID episodeId) {
        when(episodeRepository.findById(episodeId)).thenReturn(Optional.of(episode(seasonId, episodeId)));
        when(seasonRepository.findById(seasonId)).thenReturn(Optional.of(season(showId, seasonId)));
    }

    private Show show(UUID showId) {
        return Show.builder()
                .id(showId)
                .tvmazeId(1)
                .title("Test Show")
                .createdAt(Instant.now())
                .build();
    }

    private Season season(UUID showId, UUID seasonId) {
        return Season.builder().id(seasonId).showId(showId).seasonNumber(1).build();
    }

    private Episode episode(UUID seasonId, UUID episodeId) {
        return Episode.builder()
                .id(episodeId)
                .seasonId(seasonId)
                .episodeNumber(1)
                .build();
    }
}
