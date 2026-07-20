package com.tvtracker.show.dto;

import java.time.LocalDate;

public record ShowSearchResult(
        int tvmazeId, String title, String overview, String posterUrl, String tvmazeUrl, LocalDate firstAirDate) {}
