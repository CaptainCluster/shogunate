package com.tvtracker.show;

import com.tvtracker.common.TargetType;
import java.util.Collection;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserWatchStateRepository extends JpaRepository<UserWatchState, UserWatchState.UserWatchStateId> {

    @Modifying
    @Query("DELETE FROM UserWatchState u WHERE u.userId = :userId AND u.targetId IN :targetIds")
    void deleteByUserIdAndTargetIdIn(@Param("userId") UUID userId, @Param("targetIds") Collection<UUID> targetIds);

    @Modifying
    @Query(
            "DELETE FROM UserWatchState u WHERE u.userId = :userId AND u.targetType = :targetType AND u.targetId IN :targetIds")
    void deleteByUserIdAndTargetTypeAndTargetIdIn(
            @Param("userId") UUID userId,
            @Param("targetType") TargetType targetType,
            @Param("targetIds") Collection<UUID> targetIds);
}
