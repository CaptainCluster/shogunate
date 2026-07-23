package com.tvtracker.analytics;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tvtracker.auth.dto.LoginRequest;
import com.tvtracker.auth.dto.RegisterRequest;
import com.tvtracker.favorite.FavoriteRepository;
import com.tvtracker.favorite.dto.AddFavoriteRequest;
import com.tvtracker.review.ReviewRepository;
import com.tvtracker.show.EpisodeRepository;
import com.tvtracker.show.SeasonRepository;
import com.tvtracker.show.ShowRepository;
import com.tvtracker.show.UserLibraryRepository;
import com.tvtracker.show.dto.AddShowRequest;
import com.tvtracker.show.tvmaze.TvmazeClient;
import com.tvtracker.show.tvmaze.TvmazeEpisodeDto;
import com.tvtracker.show.tvmaze.TvmazeImage;
import com.tvtracker.show.tvmaze.TvmazeShowRef;
import com.tvtracker.watch.UserWatchStateRepository;
import com.tvtracker.watch.WatchEventRepository;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(properties = "spring.profiles.active=test")
@AutoConfigureMockMvc
@Testcontainers
class AnalyticsIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("shogunate")
            .withUsername("shogunate")
            .withPassword("shogunate");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WatchEventRepository watchEventRepository;

    @Autowired
    private UserWatchStateRepository userWatchStateRepository;

    @Autowired
    private UserLibraryRepository userLibraryRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private EpisodeRepository episodeRepository;

    @Autowired
    private SeasonRepository seasonRepository;

    @Autowired
    private ShowRepository showRepository;

    @MockitoBean
    private TvmazeClient tvmazeClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void resetDatabaseAndStubTvmaze() {
        watchEventRepository.deleteAll();
        userWatchStateRepository.deleteAll();
        favoriteRepository.deleteAll();
        reviewRepository.deleteAll();
        userLibraryRepository.deleteAll();
        episodeRepository.deleteAll();
        seasonRepository.deleteAll();
        showRepository.deleteAll();

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
    void markShowWatchedPopulatesTotalsAndLongestToWatch() throws Exception {
        String token = registerAndLogin("analytics_totals");
        String showId = addShow(token, 601);

        mockMvc.perform(post("/api/watch/shows/" + showId).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/analytics/totals").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.counts.episodes").value(2))
                .andExpect(jsonPath("$.counts.seasons").value(1))
                .andExpect(jsonPath("$.counts.shows").value(1));

        mockMvc.perform(get("/api/analytics/longest-to-watch").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].showId").value(showId))
                .andExpect(jsonPath("$[0].durationSeconds").value(0));
    }

    @Test
    void watchCountsFilterByCurrentMonth() throws Exception {
        String token = registerAndLogin("analytics_period");
        String showId = addShow(token, 602);

        mockMvc.perform(post("/api/watch/shows/" + showId).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        YearMonth currentMonth = YearMonth.now();
        LocalDate from = currentMonth.atDay(1);

        mockMvc.perform(get("/api/analytics/watch-counts")
                        .param("period", "MONTH")
                        .param("from", from.toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.counts.episodes").value(2))
                .andExpect(jsonPath("$.from").value(from.toString()));
    }

    @Test
    void analyticsFavoritesExcludeSuggestionsOnly() throws Exception {
        String token = registerAndLogin("analytics_fav");
        String showId = addShow(token, 603);

        mockMvc.perform(post("/api/reviews")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {"targetType":"SHOW","targetId":"%s","rating":5.0}
                                """
                                        .formatted(showId)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/analytics/favorites").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(post("/api/favorites")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddFavoriteRequest(UUID.fromString(showId)))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/analytics/favorites").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].showId").value(showId));
    }

    @Test
    void partialEpisodeWatchReportsLibraryCompletion() throws Exception {
        String token = registerAndLogin("analytics_completion");
        String showId = addShow(token, 604);

        var detail = objectMapper.readTree(
                mockMvc.perform(get("/api/shows/" + showId).header("Authorization", "Bearer " + token))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString());
        String episodeId =
                detail.get("seasons").get(0).get("episodes").get(0).get("id").asText();

        mockMvc.perform(post("/api/watch/episodes/" + episodeId).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/analytics/library-completion").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.watchedEpisodes").value(1))
                .andExpect(jsonPath("$.totalEpisodes").value(2))
                .andExpect(jsonPath("$.overallCompletionPercent").value(50.0))
                .andExpect(jsonPath("$.shows[0].completionPercent").value(50.0))
                .andExpect(jsonPath("$.shows[0].fullyWatched").value(false));
    }

    @Test
    void planToWatchCountReflectsLibraryStatus() throws Exception {
        String token = registerAndLogin("analytics_plan");
        String showId = addShow(token, 605);

        mockMvc.perform(get("/api/analytics/plan-to-watch-count").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(0));

        mockMvc.perform(patch("/api/shows/" + showId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"libraryStatus\":\"PLAN_TO_WATCH\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/analytics/plan-to-watch-count").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    void watchStreaksReportActivityAfterMarkingWatched() throws Exception {
        String token = registerAndLogin("analytics_streak");
        String showId = addShow(token, 606);

        mockMvc.perform(post("/api/watch/shows/" + showId).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/analytics/watch-streaks").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStreakDays").value(1))
                .andExpect(jsonPath("$.longestStreakDays").value(1));
    }

    @Test
    void emptyHistoryReturnsZeroAnalytics() throws Exception {
        String token = registerAndLogin("analytics_empty");
        addShow(token, 607);

        mockMvc.perform(get("/api/analytics/totals").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.counts.episodes").value(0));

        mockMvc.perform(get("/api/analytics/longest-to-watch").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/api/analytics/watch-streaks").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentStreakDays").value(0))
                .andExpect(jsonPath("$.longestStreakDays").value(0));
    }

    @Test
    void crossUserAnalyticsAreIsolated() throws Exception {
        String tokenA = registerAndLogin("analytics_user_a");
        String tokenB = registerAndLogin("analytics_user_b");
        String showId = addShow(tokenA, 608);

        mockMvc.perform(post("/api/watch/shows/" + showId).header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/analytics/totals").header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.counts.episodes").value(2));

        mockMvc.perform(get("/api/analytics/totals").header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.counts.episodes").value(0));
    }

    private String addShow(String token, int tvmazeId) throws Exception {
        MvcResult addResult = mockMvc.perform(post("/api/shows")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddShowRequest(tvmazeId))))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper
                .readTree(addResult.getResponse().getContentAsString())
                .get("id")
                .asText();
    }

    private String registerAndLogin(String username) throws Exception {
        RegisterRequest registerRequest = new RegisterRequest(username, "password123");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = new LoginRequest(username, "password123");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper
                .readTree(loginResult.getResponse().getContentAsString())
                .get("token")
                .asText();
    }
}
