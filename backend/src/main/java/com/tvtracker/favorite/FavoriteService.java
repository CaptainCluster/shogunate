package com.tvtracker.favorite;

import com.tvtracker.common.TargetType;
import com.tvtracker.common.exception.ConflictException;
import com.tvtracker.common.exception.ForbiddenException;
import com.tvtracker.common.exception.NotFoundException;
import com.tvtracker.favorite.dto.FavoriteResponse;
import com.tvtracker.favorite.dto.FavoriteStatusResponse;
import com.tvtracker.favorite.dto.FavoriteSuggestionResponse;
import com.tvtracker.review.Review;
import com.tvtracker.review.ReviewRepository;
import com.tvtracker.show.Season;
import com.tvtracker.show.SeasonRepository;
import com.tvtracker.show.ShowRepository;
import com.tvtracker.show.UserLibrary;
import com.tvtracker.show.UserLibraryRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FavoriteService {

    private final FavoriteRepository favoriteRepository;
    private final ReviewRepository reviewRepository;
    private final UserLibraryRepository userLibraryRepository;
    private final ShowRepository showRepository;
    private final SeasonRepository seasonRepository;

    @Transactional(readOnly = true)
    public List<FavoriteResponse> listFavorites(UUID userId) {
        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FavoriteSuggestionResponse> computeSuggestions(UUID userId) {
        Map<UUID, BigDecimal> scores = computeShowScores(userId);
        if (scores.isEmpty()) {
            return List.of();
        }

        BigDecimal globalMax =
                scores.values().stream().max(BigDecimal::compareTo).orElseThrow();
        Set<UUID> favoritedShowIds = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(Favorite::getShowId)
                .collect(Collectors.toSet());

        return scores.entrySet().stream()
                .filter(entry -> entry.getValue().compareTo(globalMax) == 0)
                .filter(entry -> !favoritedShowIds.contains(entry.getKey()))
                .map(entry -> new FavoriteSuggestionResponse(entry.getKey(), entry.getValue()))
                .toList();
    }

    @Transactional(readOnly = true)
    public FavoriteStatusResponse getStatus(UUID userId, UUID showId) {
        verifyShowExists(showId);
        verifyLibraryMembership(userId, showId);

        boolean isFavorite = favoriteRepository.existsByUserIdAndShowId(userId, showId);
        boolean isSuggested = computeSuggestions(userId).stream()
                .anyMatch(suggestion -> suggestion.showId().equals(showId));

        return new FavoriteStatusResponse(isFavorite, isSuggested);
    }

    @Transactional
    public FavoriteResponse addFavorite(UUID userId, UUID showId) {
        verifyShowExists(showId);
        verifyLibraryMembership(userId, showId);

        if (favoriteRepository.existsByUserIdAndShowId(userId, showId)) {
            throw new ConflictException("Show is already a favorite");
        }

        Favorite favorite = Favorite.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .showId(showId)
                .createdAt(Instant.now())
                .build();

        favoriteRepository.save(favorite);
        return toResponse(favorite);
    }

    @Transactional
    public void removeFavorite(UUID userId, UUID showId) {
        verifyShowExists(showId);

        Favorite favorite = favoriteRepository
                .findByUserIdAndShowId(userId, showId)
                .orElseThrow(() -> new NotFoundException("Favorite not found"));

        favoriteRepository.delete(favorite);
    }

    private Map<UUID, BigDecimal> computeShowScores(UUID userId) {
        Set<UUID> libraryShowIds = userLibraryRepository.findByUserIdOrderByAddedAtDesc(userId).stream()
                .map(UserLibrary::getShowId)
                .collect(Collectors.toSet());

        if (libraryShowIds.isEmpty()) {
            return Map.of();
        }

        List<Review> reviews =
                reviewRepository.findByUserIdAndTargetTypeIn(userId, List.of(TargetType.SHOW, TargetType.SEASON));

        Map<UUID, BigDecimal> scores = new HashMap<>();
        Map<UUID, Integer> seasonCountCache = new HashMap<>();

        for (Review review : reviews) {
            UUID showId;
            BigDecimal contribution;

            if (review.getTargetType() == TargetType.SHOW) {
                showId = review.getTargetId();
                if (!libraryShowIds.contains(showId)) {
                    continue;
                }
                contribution = review.getRating();
            } else {
                Season season = seasonRepository.findById(review.getTargetId()).orElse(null);
                if (season == null) {
                    continue;
                }
                showId = season.getShowId();
                if (!libraryShowIds.contains(showId)) {
                    continue;
                }
                int seasonCount = seasonCountCache.computeIfAbsent(showId, id -> seasonRepository
                        .findByShowIdOrderBySeasonNumberAsc(id)
                        .size());
                if (seasonCount == 0) {
                    continue;
                }
                contribution = review.getRating().divide(BigDecimal.valueOf(seasonCount), 1, RoundingMode.HALF_UP);
            }

            scores.merge(showId, contribution, (left, right) -> left.max(right));
        }

        return scores;
    }

    private void verifyLibraryMembership(UUID userId, UUID showId) {
        if (!userLibraryRepository.existsByUserIdAndShowId(userId, showId)) {
            throw new ForbiddenException("Show not in library");
        }
    }

    private void verifyShowExists(UUID showId) {
        if (!showRepository.existsById(showId)) {
            throw new NotFoundException("Show not found");
        }
    }

    private FavoriteResponse toResponse(Favorite favorite) {
        return new FavoriteResponse(favorite.getId(), favorite.getShowId(), favorite.getCreatedAt());
    }
}
