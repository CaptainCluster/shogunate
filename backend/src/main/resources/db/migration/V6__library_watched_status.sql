ALTER TABLE user_library
    DROP CONSTRAINT chk_user_library_status;

ALTER TABLE user_library
    ADD CONSTRAINT chk_user_library_status
        CHECK (library_status IN ('NONE', 'PLAN_TO_WATCH', 'WATCHED'));
