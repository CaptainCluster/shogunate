CREATE TABLE favorites (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    show_id UUID NOT NULL REFERENCES shows (id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL,
    UNIQUE (user_id, show_id)
);

CREATE INDEX idx_favorites_user ON favorites (user_id);
