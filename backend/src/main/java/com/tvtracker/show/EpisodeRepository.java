package com.tvtracker.show;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EpisodeRepository extends JpaRepository<Episode, UUID> {

    List<Episode> findBySeasonIdOrderByEpisodeNumberAsc(UUID seasonId);

    @Query("SELECT e FROM Episode e WHERE e.seasonId IN :seasonIds")
    List<Episode> findBySeasonIdIn(@Param("seasonIds") Collection<UUID> seasonIds);

    @Modifying
    void deleteBySeasonIdIn(Collection<UUID> seasonIds);
}
