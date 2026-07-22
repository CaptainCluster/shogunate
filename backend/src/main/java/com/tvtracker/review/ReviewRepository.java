package com.tvtracker.review;

import com.tvtracker.common.TargetType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    Optional<Review> findByUserIdAndTargetTypeAndTargetId(UUID userId, TargetType targetType, UUID targetId);

    Optional<Review> findByIdAndUserId(UUID id, UUID userId);

    List<Review> findByUserIdAndTargetTypeIn(UUID userId, Collection<TargetType> targetTypes);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Review r SET r.rating = :rating, r.body = :body WHERE r.id = :id AND r.userId = :userId")
    int updateRatingAndBody(
            @Param("id") UUID id,
            @Param("userId") UUID userId,
            @Param("rating") java.math.BigDecimal rating,
            @Param("body") String body);

    @Modifying
    @Query("DELETE FROM Review r WHERE r.userId = :userId AND r.targetId IN :targetIds")
    void deleteByUserIdAndTargetIdIn(@Param("userId") UUID userId, @Param("targetIds") Collection<UUID> targetIds);
}
