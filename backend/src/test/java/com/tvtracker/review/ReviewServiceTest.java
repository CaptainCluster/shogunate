package com.tvtracker.review;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tvtracker.common.TargetType;
import com.tvtracker.common.exception.ConflictException;
import com.tvtracker.common.exception.NotFoundException;
import com.tvtracker.common.exception.ValidationException;
import com.tvtracker.review.dto.CreateReviewRequest;
import com.tvtracker.review.dto.UpdateReviewRequest;
import com.tvtracker.show.Episode;
import com.tvtracker.show.EpisodeRepository;
import com.tvtracker.show.Season;
import com.tvtracker.show.SeasonRepository;
import com.tvtracker.show.ShowRepository;
import com.tvtracker.show.UserLibraryRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ShowRepository showRepository;

    @Mock
    private SeasonRepository seasonRepository;

    @Mock
    private EpisodeRepository episodeRepository;

    @Mock
    private UserLibraryRepository userLibraryRepository;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    void createReviewPersistsWithNullUpdatedAt() {
        UUID userId = UUID.randomUUID();
        UUID showId = UUID.randomUUID();
        UUID episodeId = UUID.randomUUID();
        UUID seasonId = UUID.randomUUID();

        stubEpisodeHierarchy(showId, seasonId, episodeId);
        when(userLibraryRepository.existsByUserIdAndShowId(userId, showId)).thenReturn(true);
        when(reviewRepository.findByUserIdAndTargetTypeAndTargetId(userId, TargetType.EPISODE, episodeId))
                .thenReturn(Optional.empty());
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = reviewService.createReview(
                userId, new CreateReviewRequest(TargetType.EPISODE, episodeId, new BigDecimal("3.5"), "Great"));

        assertEquals(new BigDecimal("3.5"), response.rating());
        assertEquals("Great", response.body());
        assertNull(response.updatedAt());
    }

    @Test
    void createReviewRejectsInvalidRatings() {
        UUID userId = UUID.randomUUID();
        UUID episodeId = UUID.randomUUID();

        assertThrows(
                ValidationException.class,
                () -> reviewService.createReview(
                        userId, new CreateReviewRequest(TargetType.EPISODE, episodeId, new BigDecimal("0.5"), null)));

        assertThrows(
                ValidationException.class,
                () -> reviewService.createReview(
                        userId, new CreateReviewRequest(TargetType.EPISODE, episodeId, new BigDecimal("3.25"), null)));

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void createReviewRejectsDuplicate() {
        UUID userId = UUID.randomUUID();
        UUID showId = UUID.randomUUID();
        UUID episodeId = UUID.randomUUID();
        UUID seasonId = UUID.randomUUID();

        stubEpisodeHierarchy(showId, seasonId, episodeId);
        when(userLibraryRepository.existsByUserIdAndShowId(userId, showId)).thenReturn(true);
        when(reviewRepository.findByUserIdAndTargetTypeAndTargetId(userId, TargetType.EPISODE, episodeId))
                .thenReturn(Optional.of(existingReview(userId, episodeId)));

        assertThrows(
                ConflictException.class,
                () -> reviewService.createReview(
                        userId, new CreateReviewRequest(TargetType.EPISODE, episodeId, new BigDecimal("4.0"), null)));

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void updateReviewNotOwnerThrowsNotFound() {
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        when(reviewRepository.findByIdAndUserId(reviewId, userId)).thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> reviewService.updateReview(
                        userId, reviewId, new UpdateReviewRequest(new BigDecimal("2.0"), "Updated")));

        verify(reviewRepository, never()).save(any());
    }

    @Test
    void deleteReviewNotOwnerThrowsNotFound() {
        UUID userId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        when(reviewRepository.findByIdAndUserId(reviewId, userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> reviewService.deleteReview(userId, reviewId));

        verify(reviewRepository, never()).delete(any());
    }

    @Test
    void ratingValidatorAcceptsValidValues() {
        RatingValidator.validate(new BigDecimal("1.0"));
        RatingValidator.validate(new BigDecimal("3.5"));
        RatingValidator.validate(new BigDecimal("5.0"));
    }

    private void stubEpisodeHierarchy(UUID showId, UUID seasonId, UUID episodeId) {
        Episode episode = Episode.builder()
                .id(episodeId)
                .seasonId(seasonId)
                .episodeNumber(1)
                .build();
        Season season =
                Season.builder().id(seasonId).showId(showId).seasonNumber(1).build();
        when(episodeRepository.findById(episodeId)).thenReturn(Optional.of(episode));
        when(seasonRepository.findById(seasonId)).thenReturn(Optional.of(season));
    }

    private Review existingReview(UUID userId, UUID episodeId) {
        return Review.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .targetType(TargetType.EPISODE)
                .targetId(episodeId)
                .rating(new BigDecimal("3.0"))
                .createdAt(Instant.now())
                .build();
    }
}
