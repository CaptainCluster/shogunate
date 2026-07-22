package com.tvtracker.favorite;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

public interface FavoriteRepository extends JpaRepository<Favorite, UUID> {

    List<Favorite> findByUserIdOrderByCreatedAtDesc(UUID userId);

    Optional<Favorite> findByUserIdAndShowId(UUID userId, UUID showId);

    boolean existsByUserIdAndShowId(UUID userId, UUID showId);

    @Modifying
    void deleteByUserIdAndShowId(UUID userId, UUID showId);
}
