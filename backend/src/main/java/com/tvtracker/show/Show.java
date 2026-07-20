package com.tvtracker.show;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "shows")
public class Show {

    @Id
    private UUID id;

    @Column(name = "tvmaze_id", nullable = false, unique = true)
    private int tvmazeId;

    @Column(nullable = false)
    private String title;

    private String overview;

    @Column(name = "poster_url")
    private String posterUrl;

    @Column(name = "tvmaze_url")
    private String tvmazeUrl;

    @Column(name = "first_air_date")
    private LocalDate firstAirDate;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Show() {}

    public Show(
            UUID id,
            int tvmazeId,
            String title,
            String overview,
            String posterUrl,
            String tvmazeUrl,
            LocalDate firstAirDate,
            Instant createdAt) {
        this.id = id;
        this.tvmazeId = tvmazeId;
        this.title = title;
        this.overview = overview;
        this.posterUrl = posterUrl;
        this.tvmazeUrl = tvmazeUrl;
        this.firstAirDate = firstAirDate;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public int getTvmazeId() {
        return tvmazeId;
    }

    public String getTitle() {
        return title;
    }

    public String getOverview() {
        return overview;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public String getTvmazeUrl() {
        return tvmazeUrl;
    }

    public LocalDate getFirstAirDate() {
        return firstAirDate;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
