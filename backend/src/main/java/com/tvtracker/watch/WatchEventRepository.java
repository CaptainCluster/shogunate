package com.tvtracker.watch;

import com.tvtracker.analytics.LongestToWatchProjection;
import com.tvtracker.analytics.ShowCompletionProjection;
import com.tvtracker.analytics.TargetTypeCountProjection;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WatchEventRepository extends JpaRepository<WatchEvent, UUID> {

    @Modifying
    @Query("DELETE FROM WatchEvent w WHERE w.userId = :userId AND w.targetId IN :targetIds")
    void deleteByUserIdAndTargetIdIn(@Param("userId") UUID userId, @Param("targetIds") Collection<UUID> targetIds);

    @Query(
            """
            SELECT w.targetType AS targetType, COUNT(w) AS count
            FROM WatchEvent w
            WHERE w.userId = :userId
              AND w.action = com.tvtracker.watch.WatchAction.WATCHED
              AND w.occurredAt >= :from
              AND w.occurredAt <= :to
            GROUP BY w.targetType
            """)
    List<TargetTypeCountProjection> countWatchedByTargetType(
            @Param("userId") UUID userId, @Param("from") Instant from, @Param("to") Instant to);

    @Query(
            """
            SELECT w.targetType AS targetType, COUNT(w) AS count
            FROM WatchEvent w
            WHERE w.userId = :userId
              AND w.action = com.tvtracker.watch.WatchAction.WATCHED
            GROUP BY w.targetType
            """)
    List<TargetTypeCountProjection> countWatchedByTargetTypeAllTime(@Param("userId") UUID userId);

    @Query(
            value =
                    """
                    SELECT s.id AS showId,
                           s.title AS title,
                           CAST(EXTRACT(EPOCH FROM (MAX(we.occurred_at) - MIN(we.occurred_at))) AS bigint) AS durationSeconds,
                           MIN(we.occurred_at) AS firstWatchedAt,
                           MAX(we.occurred_at) AS lastWatchedAt
                    FROM watch_events we
                    JOIN episodes e ON we.target_id = e.id AND we.target_type = 'EPISODE'
                    JOIN seasons se ON e.season_id = se.id
                    JOIN shows s ON se.show_id = s.id
                    JOIN user_library ul ON ul.show_id = s.id AND ul.user_id = :userId
                    WHERE we.user_id = :userId
                      AND we.action = 'WATCHED'
                    GROUP BY s.id, s.title
                    ORDER BY durationSeconds DESC
                    """,
            nativeQuery = true)
    List<LongestToWatchProjection> findLongestToWatchByShow(@Param("userId") UUID userId);

    @Query(
            value =
                    """
                    SELECT DISTINCT CAST(we.occurred_at AT TIME ZONE 'UTC' AS date) AS activityDate
                    FROM watch_events we
                    WHERE we.user_id = :userId
                      AND we.action = 'WATCHED'
                    ORDER BY activityDate
                    """,
            nativeQuery = true)
    List<LocalDate> findDistinctWatchedDates(@Param("userId") UUID userId);

    @Query(
            value =
                    """
                    SELECT s.id AS showId,
                           s.title AS title,
                           SUM(CASE WHEN uws.watched IS TRUE THEN 1 ELSE 0 END) AS watchedEpisodes,
                           COUNT(e.id) AS totalEpisodes
                    FROM user_library ul
                    JOIN shows s ON ul.show_id = s.id
                    JOIN seasons se ON se.show_id = s.id
                    JOIN episodes e ON e.season_id = se.id
                    LEFT JOIN user_watch_state uws
                           ON uws.user_id = ul.user_id
                          AND uws.target_id = e.id
                          AND uws.target_type = 'EPISODE'
                    WHERE ul.user_id = :userId
                    GROUP BY s.id, s.title
                    HAVING COUNT(e.id) > 0
                    ORDER BY (SUM(CASE WHEN uws.watched IS TRUE THEN 1 ELSE 0 END)::float / COUNT(e.id)) ASC
                    """,
            nativeQuery = true)
    List<ShowCompletionProjection> findLibraryCompletionByShow(@Param("userId") UUID userId);
}
