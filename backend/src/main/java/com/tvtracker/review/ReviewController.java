package com.tvtracker.review;

import com.tvtracker.common.TargetType;
import com.tvtracker.common.openapi.ErrorResponse;
import com.tvtracker.common.security.CurrentUser;
import com.tvtracker.review.dto.CreateReviewRequest;
import com.tvtracker.review.dto.ReviewResponse;
import com.tvtracker.review.dto.UpdateReviewRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Reviews")
@SecurityRequirement(name = "bearerAuth")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a review for an episode, season, or show")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Review created"),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid rating or validation error",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
                responseCode = "401",
                description = "Missing or invalid JWT",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
                responseCode = "403",
                description = "Show not in library",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
                responseCode = "409",
                description = "Review already exists for target",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ReviewResponse create(@Valid @RequestBody CreateReviewRequest request, @CurrentUser UUID userId) {
        return reviewService.createReview(userId, request);
    }

    @GetMapping
    @Operation(summary = "Get the authenticated user's review for a target")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Review for target"),
        @ApiResponse(
                responseCode = "401",
                description = "Missing or invalid JWT",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Review not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ReviewResponse get(
            @Parameter(description = "Review target type", required = true) @RequestParam TargetType targetType,
            @Parameter(description = "Catalog ID of the target", required = true) @RequestParam UUID targetId,
            @CurrentUser UUID userId) {
        return reviewService.getReviewByTarget(userId, targetType, targetId);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update the authenticated user's review")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Review updated"),
        @ApiResponse(
                responseCode = "400",
                description = "Invalid rating or validation error",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
                responseCode = "401",
                description = "Missing or invalid JWT",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Review not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ReviewResponse update(
            @PathVariable UUID id, @Valid @RequestBody UpdateReviewRequest request, @CurrentUser UUID userId) {
        return reviewService.updateReview(userId, id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete the authenticated user's review")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Review deleted"),
        @ApiResponse(
                responseCode = "401",
                description = "Missing or invalid JWT",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Review not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public void delete(@PathVariable UUID id, @CurrentUser UUID userId) {
        reviewService.deleteReview(userId, id);
    }
}
