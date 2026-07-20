package com.tvtracker.show;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShowRepository extends JpaRepository<Show, UUID> {

    Optional<Show> findByTvmazeId(int tvmazeId);
}
