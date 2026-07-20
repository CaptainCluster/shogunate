package com.tvtracker.show;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "seasons")
public class Season {

    @Id
    private UUID id;

    @Column(name = "show_id", nullable = false)
    private UUID showId;

    @Column(name = "season_number", nullable = false)
    private int seasonNumber;

    private String name;

    protected Season() {}

    public Season(UUID id, UUID showId, int seasonNumber, String name) {
        this.id = id;
        this.showId = showId;
        this.seasonNumber = seasonNumber;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public UUID getShowId() {
        return showId;
    }

    public int getSeasonNumber() {
        return seasonNumber;
    }

    public String getName() {
        return name;
    }
}
