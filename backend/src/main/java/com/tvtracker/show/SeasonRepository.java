package com.tvtracker.show;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

public interface SeasonRepository extends JpaRepository<Season, UUID> {

    List<Season> findByShowIdOrderBySeasonNumberAsc(UUID showId);

    @Modifying
    void deleteByShowId(UUID showId);
}
