package com.tvtracker.show;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_library")
public class UserLibrary {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "show_id", nullable = false)
    private UUID showId;

    @Enumerated(EnumType.STRING)
    @Column(name = "library_status", nullable = false)
    private LibraryStatus libraryStatus;

    @Column(name = "added_at", nullable = false)
    private Instant addedAt;

    protected UserLibrary() {}

    public UserLibrary(UUID id, UUID userId, UUID showId, LibraryStatus libraryStatus, Instant addedAt) {
        this.id = id;
        this.userId = userId;
        this.showId = showId;
        this.libraryStatus = libraryStatus;
        this.addedAt = addedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public UUID getShowId() {
        return showId;
    }

    public LibraryStatus getLibraryStatus() {
        return libraryStatus;
    }

    public Instant getAddedAt() {
        return addedAt;
    }

    public void setLibraryStatus(LibraryStatus libraryStatus) {
        this.libraryStatus = libraryStatus;
    }
}
