package com.tvtracker.show;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserLibraryRepository extends JpaRepository<UserLibrary, UUID> {

    List<UserLibrary> findByUserIdOrderByAddedAtDesc(UUID userId);

    Optional<UserLibrary> findByUserIdAndShowId(UUID userId, UUID showId);

    boolean existsByUserIdAndShowId(UUID userId, UUID showId);

    @Query("SELECT COUNT(u) > 0 FROM UserLibrary u, Show s "
            + "WHERE u.showId = s.id AND u.userId = :userId AND s.tvmazeId = :tvmazeId")
    boolean existsByUserIdAndTvmazeId(@Param("userId") UUID userId, @Param("tvmazeId") int tvmazeId);

    long countByShowId(UUID showId);

    long countByUserIdAndLibraryStatus(UUID userId, LibraryStatus libraryStatus);
}
