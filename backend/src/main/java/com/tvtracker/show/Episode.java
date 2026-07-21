package com.tvtracker.show;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "episodes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder(toBuilder = true)
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
}
