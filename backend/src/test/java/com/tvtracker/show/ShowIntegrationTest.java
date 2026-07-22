package com.tvtracker.show;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tvtracker.auth.dto.LoginRequest;
import com.tvtracker.auth.dto.RegisterRequest;
import com.tvtracker.favorite.FavoriteRepository;
import com.tvtracker.review.ReviewRepository;
import com.tvtracker.show.dto.AddShowRequest;
import com.tvtracker.show.tvmaze.TvmazeClient;
import com.tvtracker.show.tvmaze.TvmazeEpisodeDto;
import com.tvtracker.show.tvmaze.TvmazeImage;
import com.tvtracker.show.tvmaze.TvmazeShowRef;
import com.tvtracker.watch.UserWatchStateRepository;
import com.tvtracker.watch.WatchEventRepository;
import java.util.List;
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
class ShowIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("shogunate")
            .withUsername("shogunate")
            .withPassword("shogunate");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private SeasonRepository seasonRepository;

    @Autowired
    private EpisodeRepository episodeRepository;

    @Autowired
    private UserLibraryRepository userLibraryRepository;

    @Autowired
    private WatchEventRepository watchEventRepository;

    @Autowired
    private UserWatchStateRepository userWatchStateRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @MockitoBean
    private TvmazeClient tvmazeClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void resetDatabaseAndStubTvmaze() {
        favoriteRepository.deleteAll();
        reviewRepository.deleteAll();
        watchEventRepository.deleteAll();
        userWatchStateRepository.deleteAll();
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
                .thenReturn(List.of(new TvmazeEpisodeDto(1, "Pilot", 1, 1, "2008-01-20")));
    }

    @Test
    void firstAddCreatesCatalogSecondUserReusesWithoutTvmazeCall() throws Exception {
        int tvmazeId = 82;
        String userOneToken = registerAndLogin("catalog_user_one");
        String userTwoToken = registerAndLogin("catalog_user_two");

        mockMvc.perform(post("/api/shows")
                        .header("Authorization", "Bearer " + userOneToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddShowRequest(tvmazeId))))
                .andExpect(status().isCreated());

        verify(tvmazeClient, times(1)).fetchShow(tvmazeId);
        verify(tvmazeClient, times(1)).fetchEpisodes(tvmazeId);

        mockMvc.perform(post("/api/shows")
                        .header("Authorization", "Bearer " + userTwoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddShowRequest(tvmazeId))))
                .andExpect(status().isCreated());

        verify(tvmazeClient, times(1)).fetchShow(tvmazeId);
        verify(tvmazeClient, times(1)).fetchEpisodes(tvmazeId);
    }

    @Test
    void duplicateAddReturns409() throws Exception {
        int tvmazeId = 90;
        String token = registerAndLogin("duplicate_user");

        mockMvc.perform(post("/api/shows")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddShowRequest(tvmazeId))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/shows")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddShowRequest(tvmazeId))))
                .andExpect(status().isConflict());
    }

    @Test
    void removeByOneUserPreservesCatalogForOtherOrphanDeletesWhenLastRemoved() throws Exception {
        int tvmazeId = 100;
        String userOneToken = registerAndLogin("remove_user_one");
        String userTwoToken = registerAndLogin("remove_user_two");

        MvcResult addResult = mockMvc.perform(post("/api/shows")
                        .header("Authorization", "Bearer " + userOneToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddShowRequest(tvmazeId))))
                .andExpect(status().isCreated())
                .andReturn();

        String showId = objectMapper
                .readTree(addResult.getResponse().getContentAsString())
                .get("id")
                .asText();

        mockMvc.perform(post("/api/shows")
                        .header("Authorization", "Bearer " + userTwoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddShowRequest(tvmazeId))))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/shows/" + showId).header("Authorization", "Bearer " + userOneToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/shows")
                        .header("Authorization", "Bearer " + userOneToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddShowRequest(tvmazeId))))
                .andExpect(status().isCreated());

        verify(tvmazeClient, times(1)).fetchShow(tvmazeId);

        mockMvc.perform(delete("/api/shows/" + showId).header("Authorization", "Bearer " + userOneToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/shows/" + showId).header("Authorization", "Bearer " + userTwoToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/shows")
                        .header("Authorization", "Bearer " + userTwoToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddShowRequest(tvmazeId))))
                .andExpect(status().isCreated());

        verify(tvmazeClient, times(2)).fetchShow(tvmazeId);
    }

    @Test
    void getShowDetailIncludesWatchState() throws Exception {
        int tvmazeId = 110;
        String token = registerAndLogin("show_detail_watch");

        MvcResult addResult = mockMvc.perform(post("/api/shows")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddShowRequest(tvmazeId))))
                .andExpect(status().isCreated())
                .andReturn();

        var root = objectMapper.readTree(addResult.getResponse().getContentAsString());
        String showId = root.get("id").asText();
        String episodeId =
                root.get("seasons").get(0).get("episodes").get(0).get("id").asText();

        mockMvc.perform(get("/api/shows/" + showId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.watched").value(false))
                .andExpect(jsonPath("$.seasons[0].episodes[0].watched").value(false));

        mockMvc.perform(post("/api/watch/episodes/" + episodeId).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/shows/" + showId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.seasons[0].episodes[0].watched").value(true))
                .andExpect(jsonPath("$.seasons[0].episodes[0].watchedAt").exists());
    }

    @Test
    void removeFromLibraryDeletesWatchData() throws Exception {
        int tvmazeId = 111;
        String token = registerAndLogin("show_remove_watch");

        MvcResult addResult = mockMvc.perform(post("/api/shows")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddShowRequest(tvmazeId))))
                .andExpect(status().isCreated())
                .andReturn();

        var root = objectMapper.readTree(addResult.getResponse().getContentAsString());
        String showId = root.get("id").asText();
        String episodeId =
                root.get("seasons").get(0).get("episodes").get(0).get("id").asText();

        mockMvc.perform(post("/api/watch/episodes/" + episodeId).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        org.junit.jupiter.api.Assertions.assertEquals(1, watchEventRepository.count());
        org.junit.jupiter.api.Assertions.assertEquals(1, userWatchStateRepository.count());

        mockMvc.perform(delete("/api/shows/" + showId).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        org.junit.jupiter.api.Assertions.assertEquals(0, watchEventRepository.count());
        org.junit.jupiter.api.Assertions.assertEquals(0, userWatchStateRepository.count());
    }

    @Test
    void removeFromLibraryDeletesReviews() throws Exception {
        int tvmazeId = 112;
        String token = registerAndLogin("show_remove_reviews");

        MvcResult addResult = mockMvc.perform(post("/api/shows")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddShowRequest(tvmazeId))))
                .andExpect(status().isCreated())
                .andReturn();

        var root = objectMapper.readTree(addResult.getResponse().getContentAsString());
        String showId = root.get("id").asText();
        String episodeId =
                root.get("seasons").get(0).get("episodes").get(0).get("id").asText();

        mockMvc.perform(post("/api/reviews")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {"targetType":"SHOW","targetId":"%s","rating":4.0,"body":"Show review"}
                                """
                                        .formatted(showId)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/reviews")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {"targetType":"EPISODE","targetId":"%s","rating":3.5}
                                """
                                        .formatted(episodeId)))
                .andExpect(status().isCreated());

        org.junit.jupiter.api.Assertions.assertEquals(2, reviewRepository.count());

        mockMvc.perform(delete("/api/shows/" + showId).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        org.junit.jupiter.api.Assertions.assertEquals(0, reviewRepository.count());
    }

    @Test
    void removeFromLibraryDeletesFavorites() throws Exception {
        int tvmazeId = 113;
        String token = registerAndLogin("show_remove_favorites");

        MvcResult addResult = mockMvc.perform(post("/api/shows")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddShowRequest(tvmazeId))))
                .andExpect(status().isCreated())
                .andReturn();

        String showId = objectMapper
                .readTree(addResult.getResponse().getContentAsString())
                .get("id")
                .asText();

        mockMvc.perform(post("/api/favorites")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"showId\":\"" + showId + "\"}"))
                .andExpect(status().isCreated());

        org.junit.jupiter.api.Assertions.assertEquals(1, favoriteRepository.count());

        mockMvc.perform(delete("/api/shows/" + showId).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        org.junit.jupiter.api.Assertions.assertEquals(0, favoriteRepository.count());
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
