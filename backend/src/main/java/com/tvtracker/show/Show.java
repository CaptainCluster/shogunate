package com.tvtracker.show;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "shows")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(toBuilder = true)
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
}
