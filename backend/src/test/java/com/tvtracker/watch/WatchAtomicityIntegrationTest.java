package com.tvtracker.watch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import com.tvtracker.auth.User;
import com.tvtracker.auth.UserRepository;
import com.tvtracker.common.TargetType;
import com.tvtracker.show.EpisodeRepository;
import com.tvtracker.show.LibraryStatus;
import com.tvtracker.show.SeasonRepository;
import com.tvtracker.show.Show;
import com.tvtracker.show.ShowRepository;
import com.tvtracker.show.UserLibrary;
import com.tvtracker.show.UserLibraryRepository;
import com.tvtracker.show.tvmaze.TvmazeClient;
import com.tvtracker.show.tvmaze.TvmazeEpisodeDto;
import com.tvtracker.show.tvmaze.TvmazeImage;
import com.tvtracker.show.tvmaze.TvmazeShowRef;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(properties = "spring.profiles.active=test")
@Testcontainers
class WatchAtomicityIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("shogunate")
            .withUsername("shogunate")
            .withPassword("shogunate");

    @Autowired
    private WatchService watchService;

    @Autowired
    private UserWatchStateRepository userWatchStateRepository;

    @Autowired
    private UserLibraryRepository userLibraryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private SeasonRepository seasonRepository;

    @Autowired
    private EpisodeRepository episodeRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private WatchEventRepository watchEventRepository;

    @MockitoBean
    private TvmazeClient tvmazeClient;

    private UUID userId;
    private UUID showId;

    @BeforeEach
    void resetDatabaseAndStubRepositories() {
        jdbcTemplate.update("DELETE FROM watch_events");
        userWatchStateRepository.deleteAll();
        userLibraryRepository.deleteAll();
        episodeRepository.deleteAll();
        seasonRepository.deleteAll();
        showRepository.deleteAll();
        userRepository.deleteAll();

        userId = userRepository
                .save(User.builder()
                        .id(UUID.randomUUID())
                        .username("atomicity_" + UUID.randomUUID())
                        .passwordHash("hash")
                        .createdAt(Instant.now())
                        .build())
                .getId();
        showId = seedShowInLibrary();

        AtomicInteger saveCount = new AtomicInteger();
        when(watchEventRepository.save(any(WatchEvent.class))).thenAnswer(invocation -> {
            WatchEvent event = invocation.getArgument(0);
            if (saveCount.incrementAndGet() >= 2) {
                throw new DataIntegrityViolationException("simulated cascade failure");
            }
            entityManager.persist(event);
            return event;
        });

        when(tvmazeClient.fetchShow(anyInt())).thenAnswer(invocation -> {
            int id = invocation.getArgument(0);
            return new TvmazeShowRef(
                    id,
                    "Show " + id,
                    "<p>Summary</p>",
                    new TvmazeImage("poster.jpg", "poster-large.jpg"),
                    "2008-01-20",
                    "https://www.tvmaze.com/shows/" + id);
        });
        when(tvmazeClient.fetchEpisodes(anyInt()))
                .thenReturn(List.of(
                        new TvmazeEpisodeDto(1, "Pilot", 1, 1, "2008-01-20"),
                        new TvmazeEpisodeDto(2, "Second", 1, 2, "2008-01-27")));
    }

    @Test
    void markShowWatchedRollsBackStateAndEventsOnPartialFailure() {
        assertThrows(
                DataIntegrityViolationException.class, () -> watchService.markWatched(userId, TargetType.SHOW, showId));

        assertEquals(0, userWatchStateRepository.count());
        assertEquals(0L, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM watch_events", Long.class));
    }

    private UUID seedShowInLibrary() {
        Instant now = Instant.now();
        Show show = showRepository.save(Show.builder()
                .id(UUID.randomUUID())
                .tvmazeId(300)
                .title("Atomicity Show")
                .createdAt(now)
                .build());
        var season = seasonRepository.save(com.tvtracker.show.Season.builder()
                .id(UUID.randomUUID())
                .showId(show.getId())
                .seasonNumber(1)
                .build());
        episodeRepository.save(com.tvtracker.show.Episode.builder()
                .id(UUID.randomUUID())
                .seasonId(season.getId())
                .episodeNumber(1)
                .title("Pilot")
                .build());
        episodeRepository.save(com.tvtracker.show.Episode.builder()
                .id(UUID.randomUUID())
                .seasonId(season.getId())
                .episodeNumber(2)
                .title("Second")
                .build());
        userLibraryRepository.save(UserLibrary.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .showId(show.getId())
                .libraryStatus(LibraryStatus.NONE)
                .addedAt(now)
                .build());
        return show.getId();
    }
}
