CREATE TABLE shows (
    id UUID PRIMARY KEY,
    tvmaze_id INTEGER NOT NULL UNIQUE,
    title TEXT NOT NULL,
    overview TEXT,
    poster_url TEXT,
    tvmaze_url TEXT,
    first_air_date DATE,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE seasons (
    id UUID PRIMARY KEY,
    show_id UUID NOT NULL REFERENCES shows (id) ON DELETE CASCADE,
    season_number INTEGER NOT NULL,
    name TEXT,
    UNIQUE (show_id, season_number)
);

CREATE TABLE episodes (
    id UUID PRIMARY KEY,
    season_id UUID NOT NULL REFERENCES seasons (id) ON DELETE CASCADE,
    episode_number INTEGER NOT NULL,
    title TEXT,
    air_date DATE,
    UNIQUE (season_id, episode_number)
);

CREATE TABLE user_library (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    show_id UUID NOT NULL REFERENCES shows (id) ON DELETE CASCADE,
    library_status TEXT NOT NULL DEFAULT 'NONE',
    added_at TIMESTAMPTZ NOT NULL,
    UNIQUE (user_id, show_id),
    CONSTRAINT chk_user_library_status CHECK (library_status IN ('NONE', 'PLAN_TO_WATCH'))
);

CREATE TABLE user_watch_state (
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    target_type TEXT NOT NULL,
    target_id UUID NOT NULL,
    watched BOOLEAN NOT NULL DEFAULT FALSE,
    watched_at TIMESTAMPTZ,
    PRIMARY KEY (user_id, target_type, target_id),
    CONSTRAINT chk_user_watch_state_target_type CHECK (target_type IN ('EPISODE', 'SEASON', 'SHOW'))
);

CREATE INDEX idx_user_library_user_id ON user_library (user_id);
CREATE INDEX idx_seasons_show_id ON seasons (show_id);
CREATE INDEX idx_episodes_season_id ON episodes (season_id);
CREATE INDEX idx_user_watch_state_user ON user_watch_state (user_id, target_type, target_id);
