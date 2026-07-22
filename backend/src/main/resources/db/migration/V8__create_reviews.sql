CREATE TABLE reviews (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    target_type TEXT NOT NULL CHECK (target_type IN ('EPISODE', 'SEASON', 'SHOW')),
    target_id UUID NOT NULL,
    rating NUMERIC(2, 1) NOT NULL,
    body TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ,
    UNIQUE (user_id, target_type, target_id)
);

CREATE INDEX idx_reviews_user_target ON reviews (user_id, target_type, target_id);

CREATE OR REPLACE FUNCTION set_reviews_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_reviews_updated_at
    BEFORE UPDATE ON reviews
    FOR EACH ROW
    EXECUTE FUNCTION set_reviews_updated_at();
