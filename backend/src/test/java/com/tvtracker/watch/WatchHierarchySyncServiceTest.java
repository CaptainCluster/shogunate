package com.tvtracker.watch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tvtracker.common.TargetType;
import com.tvtracker.show.Episode;
import com.tvtracker.show.EpisodeRepository;
import com.tvtracker.show.Season;
import com.tvtracker.show.SeasonRepository;
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
class WatchHierarchySyncServiceTest {

    @Mock
    private SeasonRepository seasonRepository;

    @Mock
    private EpisodeRepository episodeRepository;

    @Mock
    private UserWatchStateRepository userWatchStateRepository;

    @Mock
    private WatchEventRepository watchEventRepository;

    @InjectMocks
    private WatchHierarchySyncService watchHierarchySyncService;

    @Test
    void syncAfterMarkPromotesSeasonAndShowWithLatestEpisodeTimestamp() {
        UUID userId = UUID.randomUUID();
        UUID showId = UUID.randomUUID();
        UUID seasonId = UUID.randomUUID();
        UUID firstEpisodeId = UUID.randomUUID();
        UUID secondEpisodeId = UUID.randomUUID();
        Instant firstWatchedAt = Instant.parse("2024-01-01T10:00:00Z");
        Instant secondWatchedAt = Instant.parse("2024-01-02T12:00:00Z");
        UUID sourceEventId = UUID.randomUUID();
        Instant occurredAt = Instant.parse("2024-01-02T12:00:00Z");

        when(seasonRepository.findByShowIdOrderBySeasonNumberAsc(showId)).thenReturn(List.of(season(seasonId, showId)));
        when(episodeRepository.findBySeasonIdIn(List.of(seasonId)))
                .thenReturn(List.of(episode(seasonId, firstEpisodeId, 1), episode(seasonId, secondEpisodeId, 2)));
        when(userWatchStateRepository.findByUserIdAndTargetIdIn(any(), any()))
                .thenReturn(List.of(
                        watchedState(userId, TargetType.EPISODE, firstEpisodeId, firstWatchedAt),
                        watchedState(userId, TargetType.EPISODE, secondEpisodeId, secondWatchedAt)));
        when(userWatchStateRepository.findById(any())).thenReturn(Optional.empty());
        when(userWatchStateRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(watchEventRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        watchHierarchySyncService.syncAfterWatchChange(userId, showId, sourceEventId, occurredAt, WatchAction.WATCHED);

        ArgumentCaptor<UserWatchState> stateCaptor = ArgumentCaptor.forClass(UserWatchState.class);
        verify(userWatchStateRepository, org.mockito.Mockito.times(2)).save(stateCaptor.capture());

        UserWatchState seasonState = stateCaptor.getAllValues().stream()
                .filter(state -> state.getTargetType() == TargetType.SEASON)
                .findFirst()
                .orElseThrow();
        UserWatchState showState = stateCaptor.getAllValues().stream()
                .filter(state -> state.getTargetType() == TargetType.SHOW)
                .findFirst()
                .orElseThrow();

        assertTrue(seasonState.isWatched());
        assertEquals(secondWatchedAt, seasonState.getWatchedAt());
        assertTrue(showState.isWatched());
        assertEquals(secondWatchedAt, showState.getWatchedAt());

        ArgumentCaptor<WatchEvent> eventCaptor = ArgumentCaptor.forClass(WatchEvent.class);
        verify(watchEventRepository, org.mockito.Mockito.times(2)).save(eventCaptor.capture());
        eventCaptor.getAllValues().forEach(event -> {
            assertEquals(WatchAction.WATCHED, event.getAction());
            assertTrue(event.isTriggeredByCascade());
            assertEquals(sourceEventId, event.getCascadeSourceId());
        });
    }

    @Test
    void syncAfterMarkDoesNotPromoteWhenSeasonIncomplete() {
        UUID userId = UUID.randomUUID();
        UUID showId = UUID.randomUUID();
        UUID seasonId = UUID.randomUUID();
        UUID watchedEpisodeId = UUID.randomUUID();
        UUID unwatchedEpisodeId = UUID.randomUUID();

        when(seasonRepository.findByShowIdOrderBySeasonNumberAsc(showId)).thenReturn(List.of(season(seasonId, showId)));
        when(episodeRepository.findBySeasonIdIn(List.of(seasonId)))
                .thenReturn(List.of(episode(seasonId, watchedEpisodeId, 1), episode(seasonId, unwatchedEpisodeId, 2)));
        when(userWatchStateRepository.findByUserIdAndTargetIdIn(any(), any()))
                .thenReturn(List.of(watchedState(
                        userId, TargetType.EPISODE, watchedEpisodeId, Instant.parse("2024-01-01T10:00:00Z"))));

        watchHierarchySyncService.syncAfterWatchChange(
                userId, showId, UUID.randomUUID(), Instant.now(), WatchAction.WATCHED);

        verify(userWatchStateRepository, never()).save(any());
        verify(watchEventRepository, never()).save(any());
    }

    @Test
    void syncAfterUnmarkDemotesSeasonAndShowWhenEpisodeUnwatched() {
        UUID userId = UUID.randomUUID();
        UUID showId = UUID.randomUUID();
        UUID seasonId = UUID.randomUUID();
        UUID watchedEpisodeId = UUID.randomUUID();
        UUID unwatchedEpisodeId = UUID.randomUUID();
        UUID sourceEventId = UUID.randomUUID();
        Instant occurredAt = Instant.now();

        when(seasonRepository.findByShowIdOrderBySeasonNumberAsc(showId)).thenReturn(List.of(season(seasonId, showId)));
        when(episodeRepository.findBySeasonIdIn(List.of(seasonId)))
                .thenReturn(List.of(episode(seasonId, watchedEpisodeId, 1), episode(seasonId, unwatchedEpisodeId, 2)));
        when(userWatchStateRepository.findByUserIdAndTargetIdIn(any(), any()))
                .thenReturn(List.of(
                        watchedState(userId, TargetType.EPISODE, watchedEpisodeId, occurredAt),
                        watchedState(userId, TargetType.SEASON, seasonId, occurredAt),
                        watchedState(userId, TargetType.SHOW, showId, occurredAt)));
        when(userWatchStateRepository.findById(any())).thenAnswer(invocation -> {
            UserWatchState.UserWatchStateId id = invocation.getArgument(0);
            return Optional.of(UserWatchState.builder()
                    .userId(userId)
                    .targetType(id.getTargetType())
                    .targetId(id.getTargetId())
                    .watched(true)
                    .watchedAt(occurredAt)
                    .build());
        });
        when(userWatchStateRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(watchEventRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        watchHierarchySyncService.syncAfterWatchChange(
                userId, showId, sourceEventId, occurredAt, WatchAction.UNWATCHED);

        ArgumentCaptor<UserWatchState> stateCaptor = ArgumentCaptor.forClass(UserWatchState.class);
        verify(userWatchStateRepository, org.mockito.Mockito.times(2)).save(stateCaptor.capture());
        stateCaptor.getAllValues().forEach(state -> {
            assertFalse(state.isWatched());
            assertEquals(null, state.getWatchedAt());
        });

        ArgumentCaptor<WatchEvent> eventCaptor = ArgumentCaptor.forClass(WatchEvent.class);
        verify(watchEventRepository, org.mockito.Mockito.times(2)).save(eventCaptor.capture());
        eventCaptor.getAllValues().forEach(event -> {
            assertEquals(WatchAction.UNWATCHED, event.getAction());
            assertTrue(event.isTriggeredByCascade());
            assertEquals(sourceEventId, event.getCascadeSourceId());
        });
    }

    private Season season(UUID seasonId, UUID showId) {
        return Season.builder().id(seasonId).showId(showId).seasonNumber(1).build();
    }

    private Episode episode(UUID seasonId, UUID episodeId, int episodeNumber) {
        return Episode.builder()
                .id(episodeId)
                .seasonId(seasonId)
                .episodeNumber(episodeNumber)
                .build();
    }

    private UserWatchState watchedState(UUID userId, TargetType targetType, UUID targetId, Instant watchedAt) {
        return UserWatchState.builder()
                .userId(userId)
                .targetType(targetType)
                .targetId(targetId)
                .watched(true)
                .watchedAt(watchedAt)
                .build();
    }
}
