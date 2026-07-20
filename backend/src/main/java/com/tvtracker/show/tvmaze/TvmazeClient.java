package com.tvtracker.show.tvmaze;

import java.util.List;

public interface TvmazeClient {

    List<TvmazeSearchResult> searchShows(String query);

    TvmazeShowRef fetchShow(int tvmazeId);

    List<TvmazeEpisodeDto> fetchEpisodes(int tvmazeId);
}
