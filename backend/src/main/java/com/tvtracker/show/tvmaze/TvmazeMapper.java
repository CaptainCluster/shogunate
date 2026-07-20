package com.tvtracker.show.tvmaze;

import com.tvtracker.show.Episode;
import com.tvtracker.show.Season;
import com.tvtracker.show.Show;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class TvmazeMapper {

    private TvmazeMapper() {}

    public static String stripHtml(String html) {
        if (html == null || html.isBlank()) {
            return null;
        }
        return html.replaceAll("<[^>]+>", "").trim();
    }

    public static Show toShow(TvmazeShowRef ref, UUID id, Instant createdAt) {
        return new Show(
                id,
                ref.id(),
                ref.name(),
                stripHtml(ref.summary()),
                ref.image() != null ? ref.image().medium() : null,
                ref.url(),
                parseDate(ref.premiered()),
                createdAt);
    }

    public static CatalogSnapshot toCatalogSnapshot(
            TvmazeShowRef showRef, List<TvmazeEpisodeDto> episodes, Instant now) {
        UUID showId = UUID.randomUUID();
        Show show = toShow(showRef, showId, now);

        Map<Integer, UUID> seasonIds = new LinkedHashMap<>();
        Map<Integer, Season> seasons = new LinkedHashMap<>();
        List<Episode> episodeEntities = new ArrayList<>();

        episodes.stream()
                .sorted(Comparator.comparingInt(TvmazeEpisodeDto::season)
                        .thenComparing(ep -> ep.number() != null ? ep.number() : 0))
                .forEach(ep -> {
                    UUID seasonId = seasonIds.computeIfAbsent(ep.season(), ignored -> {
                        UUID id = UUID.randomUUID();
                        seasons.put(ep.season(), new Season(id, showId, ep.season(), "Season " + ep.season()));
                        return id;
                    });
                    int episodeNumber = ep.number() != null ? ep.number() : 0;
                    episodeEntities.add(new Episode(
                            UUID.randomUUID(), seasonId, episodeNumber, ep.name(), parseDate(ep.airDate())));
                });

        return new CatalogSnapshot(show, List.copyOf(seasons.values()), episodeEntities);
    }

    public static LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return LocalDate.parse(value);
    }

    public record CatalogSnapshot(Show show, List<Season> seasons, List<Episode> episodes) {}
}
