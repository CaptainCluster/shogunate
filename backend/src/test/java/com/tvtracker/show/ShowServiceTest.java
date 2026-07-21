package com.tvtracker.show;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tvtracker.common.exception.ConflictException;
import com.tvtracker.show.tvmaze.TvmazeClient;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
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

        showService.addToLibrary(userId, 82);

        verify(tvmazeClient, never()).fetchShow(anyInt());
        verify(userLibraryRepository).save(any(UserLibrary.class));
    }
}
