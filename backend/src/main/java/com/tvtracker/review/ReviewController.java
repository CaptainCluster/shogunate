package com.tvtracker.review;

import com.tvtracker.common.TargetType;
import com.tvtracker.common.security.CurrentUser;
import com.tvtracker.review.dto.CreateReviewRequest;
import com.tvtracker.review.dto.ReviewResponse;
import com.tvtracker.review.dto.UpdateReviewRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a review for an episode, season, or show")
    public ReviewResponse create(@Valid @RequestBody CreateReviewRequest request, @CurrentUser UUID userId) {
        return reviewService.createReview(userId, request);
    }

    @GetMapping
    @Operation(summary = "Get the authenticated user's review for a target")
    public ReviewResponse get(
            @RequestParam TargetType targetType, @RequestParam UUID targetId, @CurrentUser UUID userId) {
        return reviewService.getReviewByTarget(userId, targetType, targetId);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update the authenticated user's review")
    public ReviewResponse update(
            @PathVariable UUID id, @Valid @RequestBody UpdateReviewRequest request, @CurrentUser UUID userId) {
        return reviewService.updateReview(userId, id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete the authenticated user's review")
    public void delete(@PathVariable UUID id, @CurrentUser UUID userId) {
        reviewService.deleteReview(userId, id);
    }
}
