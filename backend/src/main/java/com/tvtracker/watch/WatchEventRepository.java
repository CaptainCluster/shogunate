package com.tvtracker.watch;

import java.util.Collection;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WatchEventRepository extends JpaRepository<WatchEvent, UUID> {

    @Modifying
    @Query("DELETE FROM WatchEvent w WHERE w.userId = :userId AND w.targetId IN :targetIds")
    void deleteByUserIdAndTargetIdIn(@Param("userId") UUID userId, @Param("targetIds") Collection<UUID> targetIds);
}
