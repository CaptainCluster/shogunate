DROP TABLE IF EXISTS email_verification_tokens;
DROP TABLE IF EXISTS password_reset_tokens;

TRUNCATE TABLE users;

DROP INDEX IF EXISTS idx_users_email;
ALTER TABLE users DROP COLUMN email_verified;
ALTER TABLE users DROP COLUMN email;

ALTER TABLE users ADD COLUMN username TEXT NOT NULL;
CREATE UNIQUE INDEX idx_users_username ON users (username);
