package com.tvtracker.analytics;

import java.util.UUID;

public interface ShowCompletionProjection {

    UUID getShowId();

    String getTitle();

    long getWatchedEpisodes();

    long getTotalEpisodes();
}
