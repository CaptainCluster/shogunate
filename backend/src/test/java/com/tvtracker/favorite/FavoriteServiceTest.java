package com.tvtracker.favorite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tvtracker.common.TargetType;
import com.tvtracker.common.exception.ConflictException;
import com.tvtracker.common.exception.ForbiddenException;
import com.tvtracker.common.exception.NotFoundException;
import com.tvtracker.review.Review;
import com.tvtracker.review.ReviewRepository;
import com.tvtracker.show.LibraryStatus;
import com.tvtracker.show.Season;
import com.tvtracker.show.SeasonRepository;
import com.tvtracker.show.ShowRepository;
import com.tvtracker.show.UserLibrary;
import com.tvtracker.show.UserLibraryRepository;
import java.math.BigDecimal;
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
class FavoriteServiceTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserLibraryRepository userLibraryRepository;

    @Mock
    private ShowRepository showRepository;

    @Mock
    private SeasonRepository seasonRepository;

    @InjectMocks
    private FavoriteService favoriteService;

    @Test
    void seasonReviewWeightedBySeasonCount() {
        UUID userId = UUID.randomUUID();
        UUID showId = UUID.randomUUID();
        UUID seasonId = UUID.randomUUID();

        stubLibrary(userId, showId);
        stubFiveSeasons(showId, seasonId);
        when(reviewRepository.findByUserIdAndTargetTypeIn(userId, List.of(TargetType.SHOW, TargetType.SEASON)))
                .thenReturn(List.of(seasonReview(userId, seasonId, "5.0")));
        when(favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of());

        var suggestions = favoriteService.computeSuggestions(userId);

        assertEquals(1, suggestions.size());
        assertEquals(showId, suggestions.get(0).showId());
        assertEquals(new BigDecimal("1.0"), suggestions.get(0).weightedScore());
    }

    @Test
    void showReviewOutweighsSeasonReviewOnSameShow() {
        UUID userId = UUID.randomUUID();
        UUID showId = UUID.randomUUID();
        UUID seasonId = UUID.randomUUID();

        stubLibrary(userId, showId);
        stubFiveSeasons(showId, seasonId);
        when(reviewRepository.findByUserIdAndTargetTypeIn(userId, List.of(TargetType.SHOW, TargetType.SEASON)))
                .thenReturn(List.of(showReview(userId, showId, "3.0"), seasonReview(userId, seasonId, "5.0")));
        when(favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of());

        var suggestions = favoriteService.computeSuggestions(userId);

        assertEquals(1, suggestions.size());
        assertEquals(new BigDecimal("3.0"), suggestions.get(0).weightedScore());
    }

    @Test
    void eightSeasonShowUsesEighthWeight() {
        UUID userId = UUID.randomUUID();
        UUID showId = UUID.randomUUID();
        UUID seasonId = UUID.randomUUID();

        stubLibrary(userId, showId);
        when(seasonRepository.findByShowIdOrderBySeasonNumberAsc(showId))
                .thenReturn(List.of(
                        season(showId, seasonId, 1),
                        season(showId, UUID.randomUUID(), 2),
                        season(showId, UUID.randomUUID(), 3),
                        season(showId, UUID.randomUUID(), 4),
                        season(showId, UUID.randomUUID(), 5),
                        season(showId, UUID.randomUUID(), 6),
                        season(showId, UUID.randomUUID(), 7),
                        season(showId, UUID.randomUUID(), 8)));
        when(seasonRepository.findById(seasonId)).thenReturn(Optional.of(season(showId, seasonId, 1)));
        when(reviewRepository.findByUserIdAndTargetTypeIn(userId, List.of(TargetType.SHOW, TargetType.SEASON)))
                .thenReturn(List.of(seasonReview(userId, seasonId, "4.0")));
        when(favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of());

        var suggestions = favoriteService.computeSuggestions(userId);

        assertEquals(new BigDecimal("0.5"), suggestions.get(0).weightedScore());
    }

    @Test
    void globalMaxTiesReturnMultipleShows() {
        UUID userId = UUID.randomUUID();
        UUID showA = UUID.randomUUID();
        UUID showB = UUID.randomUUID();

        stubLibrary(userId, showA, showB);
        when(reviewRepository.findByUserIdAndTargetTypeIn(userId, List.of(TargetType.SHOW, TargetType.SEASON)))
                .thenReturn(List.of(showReview(userId, showA, "4.0"), showReview(userId, showB, "4.0")));
        when(favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of());

        var suggestions = favoriteService.computeSuggestions(userId);

        assertEquals(2, suggestions.size());
    }

    @Test
    void favoritedShowExcludedFromSuggestions() {
        UUID userId = UUID.randomUUID();
        UUID showId = UUID.randomUUID();

        stubLibrary(userId, showId);
        when(reviewRepository.findByUserIdAndTargetTypeIn(userId, List.of(TargetType.SHOW, TargetType.SEASON)))
                .thenReturn(List.of(showReview(userId, showId, "5.0")));
        when(favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(Favorite.builder()
                        .id(UUID.randomUUID())
                        .userId(userId)
                        .showId(showId)
                        .createdAt(Instant.now())
                        .build()));

        assertTrue(favoriteService.computeSuggestions(userId).isEmpty());
    }

    @Test
    void anotherUsersReviewsDoNotAffectSuggestions() {
        UUID userA = UUID.randomUUID();
        UUID userB = UUID.randomUUID();
        UUID showId = UUID.randomUUID();

        stubLibrary(userA, showId);
        stubLibrary(userB, showId);
        when(reviewRepository.findByUserIdAndTargetTypeIn(userA, List.of(TargetType.SHOW, TargetType.SEASON)))
                .thenReturn(List.of(showReview(userA, showId, "2.0")));
        when(reviewRepository.findByUserIdAndTargetTypeIn(userB, List.of(TargetType.SHOW, TargetType.SEASON)))
                .thenReturn(List.of(showReview(userB, showId, "5.0")));
        when(favoriteRepository.findByUserIdOrderByCreatedAtDesc(userA)).thenReturn(List.of());
        when(favoriteRepository.findByUserIdOrderByCreatedAtDesc(userB)).thenReturn(List.of());

        assertEquals(
                new BigDecimal("2.0"),
                favoriteService.computeSuggestions(userA).get(0).weightedScore());
        assertEquals(
                new BigDecimal("5.0"),
                favoriteService.computeSuggestions(userB).get(0).weightedScore());
    }

    @Test
    void addFavoriteRejectsDuplicate() {
        UUID userId = UUID.randomUUID();
        UUID showId = UUID.randomUUID();

        when(showRepository.existsById(showId)).thenReturn(true);
        when(userLibraryRepository.existsByUserIdAndShowId(userId, showId)).thenReturn(true);
        when(favoriteRepository.existsByUserIdAndShowId(userId, showId)).thenReturn(true);

        assertThrows(ConflictException.class, () -> favoriteService.addFavorite(userId, showId));
    }

    @Test
    void addFavoriteRequiresLibraryMembership() {
        UUID userId = UUID.randomUUID();
        UUID showId = UUID.randomUUID();

        when(showRepository.existsById(showId)).thenReturn(true);
        when(userLibraryRepository.existsByUserIdAndShowId(userId, showId)).thenReturn(false);

        assertThrows(ForbiddenException.class, () -> favoriteService.addFavorite(userId, showId));
    }

    @Test
    void removeFavoriteDeletesRow() {
        UUID userId = UUID.randomUUID();
        UUID showId = UUID.randomUUID();
        Favorite favorite = Favorite.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .showId(showId)
                .createdAt(Instant.now())
                .build();

        when(showRepository.existsById(showId)).thenReturn(true);
        when(favoriteRepository.findByUserIdAndShowId(userId, showId)).thenReturn(Optional.of(favorite));

        favoriteService.removeFavorite(userId, showId);

        verify(favoriteRepository).delete(favorite);
    }

    @Test
    void removeFavoriteNotFound() {
        UUID userId = UUID.randomUUID();
        UUID showId = UUID.randomUUID();

        when(showRepository.existsById(showId)).thenReturn(true);
        when(favoriteRepository.findByUserIdAndShowId(userId, showId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> favoriteService.removeFavorite(userId, showId));
    }

    @Test
    void getStatusReflectsFavoriteAndSuggestion() {
        UUID userId = UUID.randomUUID();
        UUID showId = UUID.randomUUID();

        when(showRepository.existsById(showId)).thenReturn(true);
        when(userLibraryRepository.existsByUserIdAndShowId(userId, showId)).thenReturn(true);
        when(favoriteRepository.existsByUserIdAndShowId(userId, showId)).thenReturn(true);

        stubLibrary(userId, showId);
        when(reviewRepository.findByUserIdAndTargetTypeIn(userId, List.of(TargetType.SHOW, TargetType.SEASON)))
                .thenReturn(List.of(showReview(userId, showId, "5.0")));
        when(favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(Favorite.builder()
                        .id(UUID.randomUUID())
                        .userId(userId)
                        .showId(showId)
                        .createdAt(Instant.now())
                        .build()));

        var status = favoriteService.getStatus(userId, showId);

        assertTrue(status.isFavorite());
        assertFalse(status.isSuggested());
    }

    private void stubLibrary(UUID userId, UUID... showIds) {
        List<UserLibrary> entries = java.util.Arrays.stream(showIds)
                .map(showId -> UserLibrary.builder()
                        .id(UUID.randomUUID())
                        .userId(userId)
                        .showId(showId)
                        .libraryStatus(LibraryStatus.NONE)
                        .addedAt(Instant.now())
                        .build())
                .toList();
        when(userLibraryRepository.findByUserIdOrderByAddedAtDesc(userId)).thenReturn(entries);
    }

    private void stubFiveSeasons(UUID showId, UUID ratedSeasonId) {
        when(seasonRepository.findByShowIdOrderBySeasonNumberAsc(showId))
                .thenReturn(List.of(
                        season(showId, ratedSeasonId, 1),
                        season(showId, UUID.randomUUID(), 2),
                        season(showId, UUID.randomUUID(), 3),
                        season(showId, UUID.randomUUID(), 4),
                        season(showId, UUID.randomUUID(), 5)));
        when(seasonRepository.findById(ratedSeasonId)).thenReturn(Optional.of(season(showId, ratedSeasonId, 1)));
    }

    private Season season(UUID showId, UUID seasonId, int number) {
        return Season.builder().id(seasonId).showId(showId).seasonNumber(number).build();
    }

    private Review showReview(UUID userId, UUID showId, String rating) {
        return Review.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .targetType(TargetType.SHOW)
                .targetId(showId)
                .rating(new BigDecimal(rating))
                .createdAt(Instant.now())
                .build();
    }

    private Review seasonReview(UUID userId, UUID seasonId, String rating) {
        return Review.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .targetType(TargetType.SEASON)
                .targetId(seasonId)
                .rating(new BigDecimal(rating))
                .createdAt(Instant.now())
                .build();
    }
}
