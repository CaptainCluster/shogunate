package com.tvtracker.review;

import com.tvtracker.common.TargetType;
import com.tvtracker.common.exception.ConflictException;
import com.tvtracker.common.exception.ForbiddenException;
import com.tvtracker.common.exception.NotFoundException;
import com.tvtracker.review.dto.CreateReviewRequest;
import com.tvtracker.review.dto.ReviewResponse;
import com.tvtracker.review.dto.UpdateReviewRequest;
import com.tvtracker.show.Episode;
import com.tvtracker.show.EpisodeRepository;
import com.tvtracker.show.Season;
import com.tvtracker.show.SeasonRepository;
import com.tvtracker.show.ShowRepository;
import com.tvtracker.show.UserLibraryRepository;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ShowRepository showRepository;
    private final SeasonRepository seasonRepository;
    private final EpisodeRepository episodeRepository;
    private final UserLibraryRepository userLibraryRepository;

    @Transactional
    public ReviewResponse createReview(UUID userId, CreateReviewRequest request) {
        RatingValidator.validate(request.rating());

        UUID showId = resolveShowId(request.targetType(), request.targetId());
        verifyLibraryMembership(userId, showId);

        if (reviewRepository
                .findByUserIdAndTargetTypeAndTargetId(userId, request.targetType(), request.targetId())
                .isPresent()) {
            throw new ConflictException("Review already exists for this target");
        }

        Review review = Review.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .targetType(request.targetType())
                .targetId(request.targetId())
                .rating(request.rating())
                .body(request.body())
                .createdAt(Instant.now())
                .updatedAt(null)
                .build();

        reviewRepository.save(review);
        return toResponse(review);
    }

    @Transactional(readOnly = true)
    public ReviewResponse getReviewByTarget(UUID userId, TargetType targetType, UUID targetId) {
        UUID showId = resolveShowId(targetType, targetId);
        verifyLibraryMembership(userId, showId);

        return reviewRepository
                .findByUserIdAndTargetTypeAndTargetId(userId, targetType, targetId)
                .map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("Review not found"));
    }

    @Transactional
    public ReviewResponse updateReview(UUID userId, UUID reviewId, UpdateReviewRequest request) {
        RatingValidator.validate(request.rating());

        Review review = reviewRepository
                .findByIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new NotFoundException("Review not found"));

        UUID showId = resolveShowId(review.getTargetType(), review.getTargetId());
        verifyLibraryMembership(userId, showId);

        int updated = reviewRepository.updateRatingAndBody(reviewId, userId, request.rating(), request.body());
        if (updated == 0) {
            throw new NotFoundException("Review not found");
        }

        return reviewRepository
                .findByIdAndUserId(reviewId, userId)
                .map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("Review not found"));
    }

    @Transactional
    public void deleteReview(UUID userId, UUID reviewId) {
        Review review = reviewRepository
                .findByIdAndUserId(reviewId, userId)
                .orElseThrow(() -> new NotFoundException("Review not found"));

        UUID showId = resolveShowId(review.getTargetType(), review.getTargetId());
        verifyLibraryMembership(userId, showId);

        reviewRepository.delete(review);
    }

    private UUID resolveShowId(TargetType targetType, UUID targetId) {
        return switch (targetType) {
            case SHOW -> showRepository
                    .findById(targetId)
                    .orElseThrow(() -> new NotFoundException("Show not found"))
                    .getId();
            case SEASON -> {
                Season season = seasonRepository
                        .findById(targetId)
                        .orElseThrow(() -> new NotFoundException("Season not found"));
                yield season.getShowId();
            }
            case EPISODE -> {
                Episode episode = episodeRepository
                        .findById(targetId)
                        .orElseThrow(() -> new NotFoundException("Episode not found"));
                Season season = seasonRepository
                        .findById(episode.getSeasonId())
                        .orElseThrow(() -> new NotFoundException("Season not found"));
                yield season.getShowId();
            }
        };
    }

    private void verifyLibraryMembership(UUID userId, UUID showId) {
        if (!userLibraryRepository.existsByUserIdAndShowId(userId, showId)) {
            throw new ForbiddenException("Show not in library");
        }
    }

    private ReviewResponse toResponse(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getTargetType(),
                review.getTargetId(),
                review.getRating(),
                review.getBody(),
                review.getCreatedAt(),
                review.getUpdatedAt());
    }
}
