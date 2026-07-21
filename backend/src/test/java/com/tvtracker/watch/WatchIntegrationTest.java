package com.tvtracker.watch;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tvtracker.auth.dto.LoginRequest;
import com.tvtracker.auth.dto.RegisterRequest;
import com.tvtracker.show.EpisodeRepository;
import com.tvtracker.show.SeasonRepository;
import com.tvtracker.show.ShowRepository;
import com.tvtracker.show.UserLibraryRepository;
import com.tvtracker.show.dto.AddShowRequest;
import com.tvtracker.show.tvmaze.TvmazeClient;
import com.tvtracker.show.tvmaze.TvmazeEpisodeDto;
import com.tvtracker.show.tvmaze.TvmazeImage;
import com.tvtracker.show.tvmaze.TvmazeShowRef;
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
class WatchIntegrationTest {

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
    void markShowWatchedCascadesAndDetailReflectsState() throws Exception {
        String token = registerAndLogin("watch_user_show");
        ShowIds ids = addShowAndExtractIds(token, 200);

        mockMvc.perform(post("/api/watch/shows/" + ids.showId()).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/shows/" + ids.showId()).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.watched").value(true))
                .andExpect(jsonPath("$.seasons[0].watched").value(true))
                .andExpect(jsonPath("$.seasons[0].episodes[0].watched").value(true))
                .andExpect(jsonPath("$.seasons[0].episodes[1].watched").value(true));

        org.junit.jupiter.api.Assertions.assertEquals(4, watchEventRepository.count());
        org.junit.jupiter.api.Assertions.assertEquals(4, userWatchStateRepository.count());
    }

    @Test
    void unmarkSeasonWithoutConfirmReturns400() throws Exception {
        String token = registerAndLogin("watch_user_confirm");
        ShowIds ids = addShowAndExtractIds(token, 201);
        String seasonId = ids.seasonId();

        mockMvc.perform(post("/api/watch/seasons/" + seasonId).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/watch/seasons/" + seasonId).header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unmarkSeasonWithConfirmClearsHierarchy() throws Exception {
        String token = registerAndLogin("watch_user_unmark");
        ShowIds ids = addShowAndExtractIds(token, 202);
        String seasonId = ids.seasonId();

        mockMvc.perform(post("/api/watch/seasons/" + seasonId).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/watch/seasons/" + seasonId + "?confirm=true")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/shows/" + ids.showId()).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.seasons[0].watched").value(false))
                .andExpect(jsonPath("$.seasons[0].episodes[0].watched").value(false));
    }

    @Test
    void crossUserCannotWatchAnotherUsersShow() throws Exception {
        String ownerToken = registerAndLogin("watch_owner");
        String otherToken = registerAndLogin("watch_other");
        ShowIds ids = addShowAndExtractIds(ownerToken, 203);

        mockMvc.perform(post("/api/watch/shows/" + ids.showId()).header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void removeShowDeletesWatchEvents() throws Exception {
        String token = registerAndLogin("watch_remove");
        ShowIds ids = addShowAndExtractIds(token, 204);

        mockMvc.perform(post("/api/watch/episodes/" + ids.episodeId()).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        org.junit.jupiter.api.Assertions.assertEquals(1, watchEventRepository.count());

        mockMvc.perform(delete("/api/shows/" + ids.showId()).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        org.junit.jupiter.api.Assertions.assertEquals(0, watchEventRepository.count());
        org.junit.jupiter.api.Assertions.assertEquals(0, userWatchStateRepository.count());
    }

    private ShowIds addShowAndExtractIds(String token, int tvmazeId) throws Exception {
        MvcResult addResult = mockMvc.perform(post("/api/shows")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddShowRequest(tvmazeId))))
                .andExpect(status().isCreated())
                .andReturn();

        var root = objectMapper.readTree(addResult.getResponse().getContentAsString());
        return new ShowIds(
                root.get("id").asText(),
                root.get("seasons").get(0).get("id").asText(),
                root.get("seasons").get(0).get("episodes").get(0).get("id").asText());
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

    private record ShowIds(String showId, String seasonId, String episodeId) {}
}
