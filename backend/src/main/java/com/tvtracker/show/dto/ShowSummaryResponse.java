package com.tvtracker.show.dto;

import com.tvtracker.show.LibraryStatus;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ShowSummaryResponse(
        UUID id,
        int tvmazeId,
        String title,
        String overview,
        String posterUrl,
        String tvmazeUrl,
        LocalDate firstAirDate,
        LibraryStatus libraryStatus,
        Instant addedAt) {}
