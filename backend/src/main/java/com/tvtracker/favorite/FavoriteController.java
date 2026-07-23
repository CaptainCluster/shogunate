package com.tvtracker.favorite;

import com.tvtracker.common.openapi.ErrorResponse;
import com.tvtracker.common.security.CurrentUser;
import com.tvtracker.favorite.dto.AddFavoriteRequest;
import com.tvtracker.favorite.dto.FavoriteResponse;
import com.tvtracker.favorite.dto.FavoriteStatusResponse;
import com.tvtracker.favorite.dto.FavoriteSuggestionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
@Tag(name = "Favorites")
@SecurityRequirement(name = "bearerAuth")
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping
    @Operation(summary = "List the authenticated user's favorite shows")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Favorite shows"),
        @ApiResponse(
                responseCode = "401",
                description = "Missing or invalid JWT",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<FavoriteResponse> list(@CurrentUser UUID userId) {
        return favoriteService.listFavorites(userId);
    }

    @GetMapping("/suggestions")
    @Operation(summary = "List show suggestions based on weighted review scores")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Suggested shows not yet favorited"),
        @ApiResponse(
                responseCode = "401",
                description = "Missing or invalid JWT",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public List<FavoriteSuggestionResponse> suggestions(@CurrentUser UUID userId) {
        return favoriteService.computeSuggestions(userId);
    }

    @GetMapping("/status")
    @Operation(summary = "Get favorite and suggestion status for a show")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Favorite and suggestion flags"),
        @ApiResponse(
                responseCode = "401",
                description = "Missing or invalid JWT",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Show not in library",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public FavoriteStatusResponse status(
            @Parameter(description = "Show catalog ID", required = true) @RequestParam UUID showId,
            @CurrentUser UUID userId) {
        return favoriteService.getStatus(userId, showId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a show to the authenticated user's favorites")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Favorite added"),
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
                description = "Show already favorited",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public FavoriteResponse add(@Valid @RequestBody AddFavoriteRequest request, @CurrentUser UUID userId) {
        return favoriteService.addFavorite(userId, request.showId());
    }

    @DeleteMapping("/{showId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove a show from the authenticated user's favorites")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Favorite removed"),
        @ApiResponse(
                responseCode = "401",
                description = "Missing or invalid JWT",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(
                responseCode = "404",
                description = "Favorite not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public void remove(@PathVariable UUID showId, @CurrentUser UUID userId) {
        favoriteService.removeFavorite(userId, showId);
    }
}
