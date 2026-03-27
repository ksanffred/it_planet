ALTER TABLE employers
    ADD COLUMN IF NOT EXISTS user_id BIGINT,
    ADD COLUMN IF NOT EXISTS description TEXT,
    ADD COLUMN IF NOT EXISTS inn VARCHAR(20),
    ADD COLUMN IF NOT EXISTS verified_org_name TEXT,
    ADD COLUMN IF NOT EXISTS status VARCHAR(50);

-- Keep only valid user_id values before adding foreign key.
UPDATE employers e
SET user_id = NULL
WHERE user_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1
      FROM users u
      WHERE u.id = e.user_id
  );

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_name = 'users'
    ) AND NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_employers_user'
    ) THEN
        ALTER TABLE employers
            ADD CONSTRAINT fk_employers_user
            FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE SET NULL;
    END IF;
END $$;

UPDATE employers
SET status = 'pending'
WHERE status IS NULL;

UPDATE employers
SET status = 'full_verified'
WHERE status = 'verified';

UPDATE employers
SET status = 'full_rejected'
WHERE status = 'rejected';

ALTER TABLE employers
    ALTER COLUMN company_name SET NOT NULL,
    ALTER COLUMN status SET NOT NULL;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_employers_status'
    ) THEN
        ALTER TABLE employers DROP CONSTRAINT chk_employers_status;
    END IF;

    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_employers_status'
    ) THEN
        ALTER TABLE employers
            ADD CONSTRAINT chk_employers_status
            CHECK (status IN ('pending', 'auto_verified', 'full_verified', 'auto_rejected', 'full_rejected'));
    END IF;
END $$;
