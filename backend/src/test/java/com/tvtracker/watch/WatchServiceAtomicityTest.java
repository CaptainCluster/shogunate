package com.tvtracker.watch;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tvtracker.common.TargetType;
import com.tvtracker.show.Episode;
import com.tvtracker.show.EpisodeRepository;
import com.tvtracker.show.LibraryStatusSyncService;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

@ExtendWith(MockitoExtension.class)
class WatchServiceAtomicityTest {

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

    @Mock
    private LibraryStatusSyncService libraryStatusSyncService;

    @InjectMocks
    private WatchService watchService;

    @Test
    void markShowWatchedRollsBackWhenEventPersistenceFails() {
        UUID userId = UUID.randomUUID();
        UUID showId = UUID.randomUUID();
        UUID seasonId = UUID.randomUUID();
        UUID episodeId = UUID.randomUUID();

        when(showRepository.findById(showId))
                .thenReturn(Optional.of(Show.builder()
                        .id(showId)
                        .tvmazeId(1)
                        .title("Show")
                        .createdAt(Instant.now())
                        .build()));
        when(userLibraryRepository.existsByUserIdAndShowId(userId, showId)).thenReturn(true);
        when(seasonRepository.findByShowIdOrderBySeasonNumberAsc(showId))
                .thenReturn(List.of(Season.builder()
                        .id(seasonId)
                        .showId(showId)
                        .seasonNumber(1)
                        .build()));
        when(episodeRepository.findBySeasonIdOrderByEpisodeNumberAsc(seasonId))
                .thenReturn(List.of(Episode.builder()
                        .id(episodeId)
                        .seasonId(seasonId)
                        .episodeNumber(1)
                        .build()));
        when(userWatchStateRepository.findById(any())).thenReturn(Optional.empty());
        when(userWatchStateRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(watchEventRepository.save(any(WatchEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0))
                .thenThrow(new DataIntegrityViolationException("simulated failure"));

        assertThrows(
                DataIntegrityViolationException.class, () -> watchService.markWatched(userId, TargetType.SHOW, showId));

        verify(userWatchStateRepository, times(3)).save(any());
        verify(watchEventRepository, times(2)).save(any());
    }
}
