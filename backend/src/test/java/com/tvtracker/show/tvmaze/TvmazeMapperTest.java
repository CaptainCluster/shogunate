package com.tvtracker.show.tvmaze;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class TvmazeMapperTest {

    @Test
    void stripHtmlRemovesTags() {
        assertEquals("Hello world", TvmazeMapper.stripHtml("<p>Hello <b>world</b></p>"));
    }

    @Test
    void stripHtmlReturnsNullForBlank() {
        assertNull(TvmazeMapper.stripHtml("   "));
    }

    @Test
    void toCatalogSnapshotGroupsEpisodesBySeason() {
        TvmazeShowRef showRef = new TvmazeShowRef(
                1,
                "Test Show",
                "<p>Summary</p>",
                new TvmazeImage("medium.jpg", "original.jpg"),
                "2020-01-01",
                "https://tvmaze.com/shows/1");

        var episodes = java.util.List.of(
                new TvmazeEpisodeDto(10, "Pilot", 1, 1, "2020-01-01"),
                new TvmazeEpisodeDto(11, "Second", 1, 2, "2020-01-08"),
                new TvmazeEpisodeDto(12, "S2E1", 2, 1, "2021-01-01"));

        TvmazeMapper.CatalogSnapshot snapshot =
                TvmazeMapper.toCatalogSnapshot(showRef, episodes, java.time.Instant.parse("2026-01-01T00:00:00Z"));

        assertEquals("Test Show", snapshot.show().getTitle());
        assertEquals("Summary", snapshot.show().getOverview());
        assertEquals(2, snapshot.seasons().size());
        assertEquals(3, snapshot.episodes().size());
    }
}
