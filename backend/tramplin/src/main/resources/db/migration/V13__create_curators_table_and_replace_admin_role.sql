CREATE TABLE IF NOT EXISTS curators (
    id       BIGSERIAL PRIMARY KEY,
    user_id  BIGINT NOT NULL UNIQUE,
    CONSTRAINT fk_curators_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'users_role_check'
    ) THEN
        ALTER TABLE users DROP CONSTRAINT users_role_check;
    END IF;

    ALTER TABLE users
        ADD CONSTRAINT users_role_check
        CHECK (role IN ('USER', 'APPLICANT', 'EMPLOYER', 'CURATOR'));
END $$;
