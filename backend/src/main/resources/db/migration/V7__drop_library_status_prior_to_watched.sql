ALTER TABLE user_library
    DROP CONSTRAINT IF EXISTS chk_user_library_status_prior;

ALTER TABLE user_library
    DROP COLUMN IF EXISTS library_status_prior_to_watched;
