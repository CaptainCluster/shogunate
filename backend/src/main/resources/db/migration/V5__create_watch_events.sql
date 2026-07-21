CREATE TABLE watch_events (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    target_type TEXT NOT NULL,
    target_id UUID NOT NULL,
    action TEXT NOT NULL CHECK (action IN ('WATCHED', 'UNWATCHED')),
    occurred_at TIMESTAMPTZ NOT NULL,
    triggered_by_cascade BOOLEAN NOT NULL DEFAULT FALSE,
    cascade_source_id UUID REFERENCES watch_events (id)
);

CREATE INDEX idx_watch_events_user_occurred ON watch_events (user_id, occurred_at);
CREATE INDEX idx_watch_events_user_target ON watch_events (user_id, target_type, target_id);
