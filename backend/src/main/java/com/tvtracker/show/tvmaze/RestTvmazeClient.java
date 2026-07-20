package com.tvtracker.show.tvmaze;

import com.tvtracker.common.exception.ApiException;
import com.tvtracker.config.TvmazeProperties;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RestTvmazeClient implements TvmazeClient {

    private static final int MAX_ATTEMPTS = 3;

    private final RestClient restClient;

    public RestTvmazeClient(TvmazeProperties properties) {
        String baseUrl = properties.baseUrl() != null ? properties.baseUrl() : "https://api.tvmaze.com";
        String userAgent = properties.userAgent() != null ? properties.userAgent() : "Shogunate/1.0";
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("User-Agent", userAgent)
                .defaultHeader("Accept", "application/json")
                .build();
    }

    @Override
    public List<TvmazeSearchResult> searchShows(String query) {
        TvmazeSearchResult[] results = exchange("/search/shows?q={query}", TvmazeSearchResult[].class, query);
        return results == null ? List.of() : Arrays.asList(results);
    }

    @Override
    public TvmazeShowRef fetchShow(int tvmazeId) {
        return exchange("/shows/{id}", TvmazeShowRef.class, tvmazeId);
    }

    @Override
    public List<TvmazeEpisodeDto> fetchEpisodes(int tvmazeId) {
        TvmazeEpisodeDto[] episodes = exchange("/shows/{id}/episodes", TvmazeEpisodeDto[].class, tvmazeId);
        return episodes == null ? List.of() : Arrays.asList(episodes);
    }

    private <T> T exchange(String path, Class<T> type, Object... uriVars) {
        int attempt = 0;
        while (true) {
            attempt++;
            try {
                return restClient.get().uri(path, uriVars).retrieve().body(type);
            } catch (org.springframework.web.client.HttpClientErrorException.TooManyRequests ex) {
                if (attempt >= MAX_ATTEMPTS) {
                    throw new ApiException("TVmaze rate limit exceeded", 503);
                }
                sleep(Duration.ofMillis(500L * attempt));
            } catch (org.springframework.web.client.RestClientException ex) {
                throw new ApiException("Failed to fetch from TVmaze: " + ex.getMessage(), 502);
            }
        }
    }

    private void sleep(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ApiException("Interrupted while waiting for TVmaze", 503);
        }
    }
}
