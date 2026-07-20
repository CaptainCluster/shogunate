package com.tvtracker.show;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "episodes")
public class Episode {

    @Id
    private UUID id;

    @Column(name = "season_id", nullable = false)
    private UUID seasonId;

    @Column(name = "episode_number", nullable = false)
    private int episodeNumber;

    private String title;

    @Column(name = "air_date")
    private LocalDate airDate;

    protected Episode() {}

    public Episode(UUID id, UUID seasonId, int episodeNumber, String title, LocalDate airDate) {
        this.id = id;
        this.seasonId = seasonId;
        this.episodeNumber = episodeNumber;
        this.title = title;
        this.airDate = airDate;
    }

    public UUID getId() {
        return id;
    }

    public UUID getSeasonId() {
        return seasonId;
    }

    public int getEpisodeNumber() {
        return episodeNumber;
    }

    public String getTitle() {
        return title;
    }

    public LocalDate getAirDate() {
        return airDate;
    }
}
