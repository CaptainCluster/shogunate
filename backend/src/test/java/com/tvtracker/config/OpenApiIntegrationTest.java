package com.tvtracker.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(properties = "spring.profiles.active=test")
@AutoConfigureMockMvc
@Testcontainers
class OpenApiIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("shogunate")
            .withUsername("shogunate")
            .withPassword("shogunate");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void openApiDocumentsAllWatchEndpoints() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/watch/episodes/{id}'].post.summary")
                        .value("Mark an episode as watched"))
                .andExpect(jsonPath("$.paths['/api/watch/episodes/{id}'].delete.summary")
                        .value("Unmark an episode as watched"))
                .andExpect(jsonPath("$.paths['/api/watch/seasons/{id}'].post.summary")
                        .value("Mark a season and its episodes as watched"))
                .andExpect(jsonPath("$.paths['/api/watch/seasons/{id}'].delete.summary")
                        .value("Unmark a season and its episodes as watched"))
                .andExpect(jsonPath("$.paths['/api/watch/shows/{id}'].post.summary")
                        .value("Mark a show and all seasons and episodes as watched"))
                .andExpect(jsonPath("$.paths['/api/watch/shows/{id}'].delete.summary")
                        .value("Unmark a show and all seasons and episodes as watched"))
                .andExpect(jsonPath("$.paths['/api/reviews'].post.summary")
                        .value("Create a review for an episode, season, or show"))
                .andExpect(jsonPath("$.paths['/api/reviews'].get.summary")
                        .value("Get the authenticated user's review for a target"))
                .andExpect(jsonPath("$.paths['/api/reviews/{id}'].put.summary")
                        .value("Update the authenticated user's review"))
                .andExpect(jsonPath("$.paths['/api/reviews/{id}'].delete.summary")
                        .value("Delete the authenticated user's review"));
    }
}
