package com.tvtracker.show;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tvtracker.common.TargetType;
import com.tvtracker.common.exception.ConflictException;
import com.tvtracker.common.exception.NotFoundException;
import com.tvtracker.show.dto.ShowDetailResponse;
import com.tvtracker.show.tvmaze.TvmazeClient;
import com.tvtracker.watch.UserWatchState;
import com.tvtracker.watch.UserWatchStateRepository;
import com.tvtracker.watch.WatchEventRepository;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShowServiceTest {

    @Mock
    private TvmazeClient tvmazeClient;

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
    private ShowService showService;

    @Test
    void addToLibraryRejectsDuplicate() {
        UUID userId = UUID.randomUUID();
        when(userLibraryRepository.existsByUserIdAndTvmazeId(userId, 82)).thenReturn(true);

        assertThrows(ConflictException.class, () -> showService.addToLibrary(userId, 82));

        verify(tvmazeClient, never()).fetchShow(anyInt());
    }

    @Test
    void addToLibraryReusesExistingCatalog() {
        UUID userId = UUID.randomUUID();
        UUID showId = UUID.randomUUID();
        Show show = Show.builder()
                .id(showId)
                .tvmazeId(82)
                .title("Breaking Bad")
                .createdAt(Instant.now())
                .build();
        UserLibrary entry = UserLibrary.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .showId(showId)
                .libraryStatus(LibraryStatus.NONE)
                .addedAt(Instant.now())
                .build();

        when(userLibraryRepository.existsByUserIdAndTvmazeId(userId, 82)).thenReturn(false);
        when(showRepository.findByTvmazeId(82)).thenReturn(Optional.of(show));
        when(userLibraryRepository.save(any(UserLibrary.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userLibraryRepository.findByUserIdAndShowId(userId, showId)).thenReturn(Optional.of(entry));
        when(showRepository.findById(showId)).thenReturn(Optional.of(show));
        when(seasonRepository.findByShowIdOrderBySeasonNumberAsc(showId)).thenReturn(List.of());
        when(userWatchStateRepository.findByUserIdAndTargetIdIn(userId, java.util.Set.of(showId)))
                .thenReturn(List.of());

        showService.addToLibrary(userId, 82);

        verify(tvmazeClient, never()).fetchShow(anyInt());
        verify(userLibraryRepository).save(any(UserLibrary.class));
    }

    @Test
    void searchBlankQueryReturnsEmptyList() {
        assertTrue(showService.search(null).isEmpty());
        assertTrue(showService.search("   ").isEmpty());
        verify(tvmazeClient, never()).searchShows(any());
    }

    @Test
    void getShowDetailIncludesWatchStateForHierarchy() {
        UUID userId = UUID.randomUUID();
        UUID showId = UUID.randomUUID();
        UUID seasonId = UUID.randomUUID();
        UUID episodeId = UUID.randomUUID();
        Instant watchedAt = Instant.parse("2024-06-01T12:00:00Z");

        Show show = Show.builder()
                .id(showId)
                .tvmazeId(1)
                .title("Test")
                .createdAt(Instant.now())
                .build();
        Season season = Season.builder()
                .id(seasonId)
                .showId(showId)
                .seasonNumber(1)
                .name("S1")
                .build();
        Episode episode = Episode.builder()
                .id(episodeId)
                .seasonId(seasonId)
                .episodeNumber(1)
                .title("Pilot")
                .airDate(LocalDate.parse("2024-01-01"))
                .build();
        UserLibrary entry = UserLibrary.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .showId(showId)
                .libraryStatus(LibraryStatus.PLAN_TO_WATCH)
                .addedAt(Instant.now())
                .build();

        when(userLibraryRepository.findByUserIdAndShowId(userId, showId)).thenReturn(Optional.of(entry));
        when(showRepository.findById(showId)).thenReturn(Optional.of(show));
        when(seasonRepository.findByShowIdOrderBySeasonNumberAsc(showId)).thenReturn(List.of(season));
        when(episodeRepository.findBySeasonIdOrderByEpisodeNumberAsc(seasonId)).thenReturn(List.of(episode));
        when(userWatchStateRepository.findByUserIdAndTargetIdIn(eq(userId), any(Set.class)))
                .thenReturn(List.of(
                        UserWatchState.builder()
                                .userId(userId)
                                .targetType(TargetType.SHOW)
                                .targetId(showId)
                                .watched(true)
                                .watchedAt(watchedAt)
                                .build(),
                        UserWatchState.builder()
                                .userId(userId)
                                .targetType(TargetType.SEASON)
                                .targetId(seasonId)
                                .watched(true)
                                .watchedAt(watchedAt)
                                .build(),
                        UserWatchState.builder()
                                .userId(userId)
                                .targetType(TargetType.EPISODE)
                                .targetId(episodeId)
                                .watched(true)
                                .watchedAt(watchedAt)
                                .build()));

        ShowDetailResponse detail = showService.getShowDetail(userId, showId);

        assertTrue(detail.watched());
        assertEquals(watchedAt, detail.watchedAt());
        assertTrue(detail.seasons().getFirst().watched());
        assertTrue(detail.seasons().getFirst().episodes().getFirst().watched());
        assertEquals(LibraryStatus.PLAN_TO_WATCH, detail.libraryStatus());
    }

    @Test
    void getShowDetailNotInLibraryThrows() {
        UUID userId = UUID.randomUUID();
        UUID showId = UUID.randomUUID();
        when(userLibraryRepository.findByUserIdAndShowId(userId, showId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> showService.getShowDetail(userId, showId));
    }

    @Test
    void removeFromLibraryDeletesWatchDataAndOrphanCatalog() {
        UUID userId = UUID.randomUUID();
        UUID showId = UUID.randomUUID();
        UUID seasonId = UUID.randomUUID();
        UUID episodeId = UUID.randomUUID();
        UserLibrary entry = UserLibrary.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .showId(showId)
                .libraryStatus(LibraryStatus.NONE)
                .addedAt(Instant.now())
                .build();

        when(userLibraryRepository.findByUserIdAndShowId(userId, showId)).thenReturn(Optional.of(entry));
        when(seasonRepository.findByShowIdOrderBySeasonNumberAsc(showId))
                .thenReturn(List.of(Season.builder()
                        .id(seasonId)
                        .showId(showId)
                        .seasonNumber(1)
                        .build()));
        when(episodeRepository.findBySeasonIdIn(List.of(seasonId)))
                .thenReturn(List.of(Episode.builder()
                        .id(episodeId)
                        .seasonId(seasonId)
                        .episodeNumber(1)
                        .build()));
        when(userLibraryRepository.countByShowId(showId)).thenReturn(0L);

        showService.removeFromLibrary(userId, showId);

        verify(userWatchStateRepository)
                .deleteByUserIdAndTargetIdIn(eq(userId), eq(Set.of(showId, seasonId, episodeId)));
        verify(watchEventRepository).deleteByUserIdAndTargetIdIn(eq(userId), eq(Set.of(showId, seasonId, episodeId)));
        verify(userLibraryRepository).delete(entry);
        verify(episodeRepository).deleteBySeasonIdIn(List.of(seasonId));
        verify(seasonRepository).deleteByShowId(showId);
        verify(showRepository).deleteById(showId);
    }

    @Test
    void listLibraryReturnsSummaries() {
        UUID userId = UUID.randomUUID();
        UUID showId = UUID.randomUUID();
        Instant addedAt = Instant.now();
        UserLibrary entry = UserLibrary.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .showId(showId)
                .libraryStatus(LibraryStatus.NONE)
                .addedAt(addedAt)
                .build();
        Show show = Show.builder()
                .id(showId)
                .tvmazeId(99)
                .title("Listed")
                .createdAt(Instant.now())
                .build();

        when(userLibraryRepository.findByUserIdOrderByAddedAtDesc(userId)).thenReturn(List.of(entry));
        when(showRepository.findById(showId)).thenReturn(Optional.of(show));

        var summaries = showService.listLibrary(userId);

        assertEquals(1, summaries.size());
        assertEquals("Listed", summaries.getFirst().title());
        assertEquals(addedAt, summaries.getFirst().addedAt());
    }

    @Test
    void updateLibraryStatusUpdatesEntry() {
        UUID userId = UUID.randomUUID();
        UUID showId = UUID.randomUUID();
        UserLibrary entry = UserLibrary.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .showId(showId)
                .libraryStatus(LibraryStatus.NONE)
                .addedAt(Instant.now())
                .build();
        Show show = Show.builder()
                .id(showId)
                .tvmazeId(5)
                .title("Status Show")
                .createdAt(Instant.now())
                .build();

        when(userLibraryRepository.findByUserIdAndShowId(userId, showId)).thenReturn(Optional.of(entry));
        when(userLibraryRepository.save(any(UserLibrary.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(showRepository.findById(showId)).thenReturn(Optional.of(show));

        var summary = showService.updateLibraryStatus(userId, showId, LibraryStatus.PLAN_TO_WATCH);

        assertEquals(LibraryStatus.PLAN_TO_WATCH, summary.libraryStatus());
    }
}
