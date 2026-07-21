package com.tvtracker.watch;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserWatchStateRepository extends JpaRepository<UserWatchState, UserWatchState.UserWatchStateId> {

    List<UserWatchState> findByUserIdAndTargetIdIn(UUID userId, Collection<UUID> targetIds);

    @Modifying
    @Query("DELETE FROM UserWatchState u WHERE u.userId = :userId AND u.targetId IN :targetIds")
    void deleteByUserIdAndTargetIdIn(@Param("userId") UUID userId, @Param("targetIds") Collection<UUID> targetIds);
}
