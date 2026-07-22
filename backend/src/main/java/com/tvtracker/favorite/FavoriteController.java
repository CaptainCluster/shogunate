package com.tvtracker.favorite;

import com.tvtracker.common.security.CurrentUser;
import com.tvtracker.favorite.dto.AddFavoriteRequest;
import com.tvtracker.favorite.dto.FavoriteResponse;
import com.tvtracker.favorite.dto.FavoriteStatusResponse;
import com.tvtracker.favorite.dto.FavoriteSuggestionResponse;
import io.swagger.v3.oas.annotations.Operation;
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
public class FavoriteController {

    private final FavoriteService favoriteService;

    @GetMapping
    @Operation(summary = "List the authenticated user's favorite shows")
    public List<FavoriteResponse> list(@CurrentUser UUID userId) {
        return favoriteService.listFavorites(userId);
    }

    @GetMapping("/suggestions")
    @Operation(summary = "List show suggestions based on weighted review scores")
    public List<FavoriteSuggestionResponse> suggestions(@CurrentUser UUID userId) {
        return favoriteService.computeSuggestions(userId);
    }

    @GetMapping("/status")
    @Operation(summary = "Get favorite and suggestion status for a show")
    public FavoriteStatusResponse status(@RequestParam UUID showId, @CurrentUser UUID userId) {
        return favoriteService.getStatus(userId, showId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a show to the authenticated user's favorites")
    public FavoriteResponse add(@Valid @RequestBody AddFavoriteRequest request, @CurrentUser UUID userId) {
        return favoriteService.addFavorite(userId, request.showId());
    }

    @DeleteMapping("/{showId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remove a show from the authenticated user's favorites")
    public void remove(@PathVariable UUID showId, @CurrentUser UUID userId) {
        favoriteService.removeFavorite(userId, showId);
    }
}
