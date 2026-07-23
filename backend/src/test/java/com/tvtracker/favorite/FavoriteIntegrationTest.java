package com.tvtracker.favorite;

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
import com.tvtracker.favorite.dto.AddFavoriteRequest;
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
class FavoriteIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("shogunate")
            .withUsername("shogunate")
            .withPassword("shogunate");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FavoriteRepository favoriteRepository;

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
        favoriteRepository.deleteAll();
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
    void favoritesAndSuggestionsAreSeparateUntilUserAdds() throws Exception {
        String token = registerAndLogin("fav_user_one");
        String showId = addShow(token, 501);

        mockMvc.perform(post("/api/reviews")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {"targetType":"SHOW","targetId":"%s","rating":5.0}
                                """
                                        .formatted(showId)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/favorites/suggestions").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].showId").value(showId))
                .andExpect(jsonPath("$[0].weightedScore").value(5.0));

        mockMvc.perform(get("/api/favorites").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(post("/api/favorites")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AddFavoriteRequest(java.util.UUID.fromString(showId)))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.showId").value(showId));

        mockMvc.perform(get("/api/favorites").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/api/favorites/suggestions").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/api/favorites/status").param("showId", showId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isFavorite").value(true))
                .andExpect(jsonPath("$.isSuggested").value(false));
    }

    @Test
    void duplicateFavoriteReturns409() throws Exception {
        String token = registerAndLogin("fav_dup");
        String showId = addShow(token, 502);

        AddFavoriteRequest request = new AddFavoriteRequest(java.util.UUID.fromString(showId));

        mockMvc.perform(post("/api/favorites")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/favorites")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void addFavoriteWithoutLibraryMembershipReturns403() throws Exception {
        String token = registerAndLogin("fav_no_lib");
        String otherToken = registerAndLogin("fav_no_lib_other");
        String showId = addShow(token, 503);

        mockMvc.perform(post("/api/favorites")
                        .header("Authorization", "Bearer " + otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AddFavoriteRequest(java.util.UUID.fromString(showId)))))
                .andExpect(status().isForbidden());
    }

    @Test
    void crossUserFavoritesAreIsolated() throws Exception {
        String tokenA = registerAndLogin("fav_user_a");
        String tokenB = registerAndLogin("fav_user_b");
        String showIdA = addShow(tokenA, 504);
        addShow(tokenB, 505);

        mockMvc.perform(post("/api/favorites")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AddFavoriteRequest(java.util.UUID.fromString(showIdA)))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/favorites").header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/api/favorites").header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void crossUserFavoriteStatusAndSuggestionsAreIsolated() throws Exception {
        String tokenA = registerAndLogin("fav_status_a");
        String tokenB = registerAndLogin("fav_status_b");
        String showIdA = addShow(tokenA, 507);
        String showIdB = addShow(tokenB, 508);

        mockMvc.perform(post("/api/favorites")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AddFavoriteRequest(java.util.UUID.fromString(showIdA)))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/reviews")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {"targetType":"SHOW","targetId":"%s","rating":5.0}
                                """
                                        .formatted(showIdA)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/favorites/status")
                        .param("showId", showIdA)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/favorites/suggestions").header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/api/favorites/status")
                        .param("showId", showIdB)
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isFavorite").value(false));

        mockMvc.perform(delete("/api/favorites/" + showIdA).header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());
    }

    @Test
    void removeFavoriteReturns204() throws Exception {
        String token = registerAndLogin("fav_remove");
        String showId = addShow(token, 506);

        mockMvc.perform(post("/api/favorites")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AddFavoriteRequest(java.util.UUID.fromString(showId)))))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/favorites/" + showId).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/favorites/" + showId).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
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
