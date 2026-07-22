package com.tvtracker.review;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tvtracker.auth.dto.LoginRequest;
import com.tvtracker.auth.dto.RegisterRequest;
import com.tvtracker.common.TargetType;
import com.tvtracker.review.dto.CreateReviewRequest;
import com.tvtracker.review.dto.UpdateReviewRequest;
import com.tvtracker.show.EpisodeRepository;
import com.tvtracker.show.SeasonRepository;
import com.tvtracker.show.ShowRepository;
import com.tvtracker.show.UserLibraryRepository;
import com.tvtracker.show.dto.AddShowRequest;
import com.tvtracker.show.tvmaze.TvmazeClient;
import com.tvtracker.show.tvmaze.TvmazeEpisodeDto;
import com.tvtracker.show.tvmaze.TvmazeImage;
import com.tvtracker.show.tvmaze.TvmazeShowRef;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(properties = "spring.profiles.active=test")
@AutoConfigureMockMvc
@Testcontainers
class ReviewIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("shogunate")
            .withUsername("shogunate")
            .withPassword("shogunate");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserLibraryRepository userLibraryRepository;

    @Autowired
    private EpisodeRepository episodeRepository;

    @Autowired
    private SeasonRepository seasonRepository;

    @Autowired
    private ShowRepository showRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private TvmazeClient tvmazeClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void resetDatabaseAndStubTvmaze() {
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

        ensureReviewsUpdatedAtTrigger();
    }

    private void ensureReviewsUpdatedAtTrigger() {
        jdbcTemplate.execute(
                """
                CREATE OR REPLACE FUNCTION set_reviews_updated_at()
                RETURNS TRIGGER AS $$
                BEGIN
                    NEW.updated_at = NOW();
                    RETURN NEW;
                END;
                $$ LANGUAGE plpgsql;
                """);
        jdbcTemplate.execute("DROP TRIGGER IF EXISTS trg_reviews_updated_at ON reviews");
        jdbcTemplate.execute(
                """
                CREATE TRIGGER trg_reviews_updated_at
                    BEFORE UPDATE ON reviews
                    FOR EACH ROW
                    EXECUTE FUNCTION set_reviews_updated_at();
                """);
    }

    @Test
    void reviewCrudAndUpdatedAtTrigger() throws Exception {
        String token = registerAndLogin("review_crud");
        ShowIds ids = addShowAndExtractIds(token, 301);

        CreateReviewRequest createRequest =
                new CreateReviewRequest(TargetType.EPISODE, ids.episodeUuid(), new BigDecimal("4.5"), "Solid pilot");

        MvcResult createResult = mockMvc.perform(post("/api/reviews")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating").value(4.5))
                .andExpect(jsonPath("$.body").value("Solid pilot"))
                .andExpect(jsonPath("$.updatedAt").doesNotExist())
                .andReturn();

        String reviewId = objectMapper
                .readTree(createResult.getResponse().getContentAsString())
                .get("id")
                .asText();

        mockMvc.perform(get("/api/reviews")
                        .param("targetType", "EPISODE")
                        .param("targetId", ids.episodeId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reviewId));

        mockMvc.perform(put("/api/reviews/" + reviewId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateReviewRequest(new BigDecimal("5.0"), "Even better on rewatch"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rating").value(5.0))
                .andExpect(jsonPath("$.updatedAt").exists());

        mockMvc.perform(delete("/api/reviews/" + reviewId).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/reviews")
                        .param("targetType", "EPISODE")
                        .param("targetId", ids.episodeId())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void duplicateCreateReturnsConflict() throws Exception {
        String token = registerAndLogin("review_dup");
        ShowIds ids = addShowAndExtractIds(token, 302);

        CreateReviewRequest request =
                new CreateReviewRequest(TargetType.SHOW, ids.showUuid(), new BigDecimal("3.0"), "Good show");

        mockMvc.perform(post("/api/reviews")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/reviews")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void crossUserCannotAccessAnotherUsersReview() throws Exception {
        String ownerToken = registerAndLogin("review_owner");
        String otherToken = registerAndLogin("review_other");
        ShowIds ids = addShowAndExtractIds(ownerToken, 303);

        addShowForUser(otherToken, 303);

        MvcResult createResult = mockMvc.perform(post("/api/reviews")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateReviewRequest(
                                TargetType.EPISODE, ids.episodeUuid(), new BigDecimal("4.0"), "Private"))))
                .andExpect(status().isCreated())
                .andReturn();

        String reviewId = objectMapper
                .readTree(createResult.getResponse().getContentAsString())
                .get("id")
                .asText();

        mockMvc.perform(get("/api/reviews")
                        .param("targetType", "EPISODE")
                        .param("targetId", ids.episodeId())
                        .header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/api/reviews/" + reviewId)
                        .header("Authorization", "Bearer " + otherToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new UpdateReviewRequest(new BigDecimal("1.0"), "Hacked"))))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/api/reviews/" + reviewId).header("Authorization", "Bearer " + otherToken))
                .andExpect(status().isNotFound());

        org.junit.jupiter.api.Assertions.assertEquals(1, reviewRepository.count());
    }

    @Test
    void reviewRejectedWhenShowNotInLibrary() throws Exception {
        String ownerToken = registerAndLogin("review_no_lib_owner");
        String token = registerAndLogin("review_no_lib");
        ShowIds ids = addShowAndExtractIds(ownerToken, 304);
        addShowForUser(token, 304);

        mockMvc.perform(delete("/api/shows/" + ids.showId()).header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/reviews")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateReviewRequest(
                                TargetType.EPISODE, ids.episodeUuid(), new BigDecimal("2.0"), null))))
                .andExpect(status().isForbidden());
    }

    @Test
    void invalidRatingReturnsBadRequest() throws Exception {
        String token = registerAndLogin("review_invalid");
        ShowIds ids = addShowAndExtractIds(token, 305);

        ObjectNode body = objectMapper.createObjectNode();
        body.put("targetType", "EPISODE");
        body.put("targetId", ids.episodeId());
        body.put("rating", 0.5);

        mockMvc.perform(post("/api/reviews")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.toString()))
                .andExpect(status().isBadRequest());
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

    private void addShowForUser(String token, int tvmazeId) throws Exception {
        mockMvc.perform(post("/api/shows")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddShowRequest(tvmazeId))))
                .andExpect(status().isCreated());
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

    private record ShowIds(String showId, String seasonId, String episodeId) {
        java.util.UUID showUuid() {
            return java.util.UUID.fromString(showId);
        }

        java.util.UUID episodeUuid() {
            return java.util.UUID.fromString(episodeId);
        }
    }
}
