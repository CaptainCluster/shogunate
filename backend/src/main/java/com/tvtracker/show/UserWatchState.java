package com.tvtracker.show;

import com.tvtracker.common.TargetType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "user_watch_state")
@IdClass(UserWatchState.UserWatchStateId.class)
public class UserWatchState {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type")
    private TargetType targetType;

    @Id
    @Column(name = "target_id")
    private UUID targetId;

    @Column(nullable = false)
    private boolean watched;

    @Column(name = "watched_at")
    private Instant watchedAt;

    protected UserWatchState() {}

    public UserWatchState(UUID userId, TargetType targetType, UUID targetId, boolean watched, Instant watchedAt) {
        this.userId = userId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.watched = watched;
        this.watchedAt = watchedAt;
    }

    public UUID getUserId() {
        return userId;
    }

    public TargetType getTargetType() {
        return targetType;
    }

    public UUID getTargetId() {
        return targetId;
    }

    public boolean isWatched() {
        return watched;
    }

    public Instant getWatchedAt() {
        return watchedAt;
    }

    public static class UserWatchStateId implements Serializable {

        private UUID userId;
        private TargetType targetType;
        private UUID targetId;

        public UserWatchStateId() {}

        public UserWatchStateId(UUID userId, TargetType targetType, UUID targetId) {
            this.userId = userId;
            this.targetType = targetType;
            this.targetId = targetId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof UserWatchStateId that)) {
                return false;
            }
            return Objects.equals(userId, that.userId)
                    && targetType == that.targetType
                    && Objects.equals(targetId, that.targetId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, targetType, targetId);
        }
    }
}
