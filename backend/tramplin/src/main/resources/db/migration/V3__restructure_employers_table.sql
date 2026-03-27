DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'employers'
          AND column_name = 'name'
    ) THEN
        ALTER TABLE employers RENAME COLUMN name TO company_name;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'employers'
          AND column_name = 'contacts'
    ) THEN
        ALTER TABLE employers RENAME COLUMN contacts TO socials;
    END IF;
END $$;

ALTER TABLE employers
    ADD COLUMN IF NOT EXISTS user_id BIGINT,
    ADD COLUMN IF NOT EXISTS description TEXT,
    ADD COLUMN IF NOT EXISTS inn VARCHAR(20),
    ADD COLUMN IF NOT EXISTS status VARCHAR(50);

UPDATE employers
SET status = 'pending'
WHERE status IS NULL;

ALTER TABLE employers
    ALTER COLUMN company_name SET NOT NULL,
    ALTER COLUMN status SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_employers_status'
    ) THEN
        ALTER TABLE employers
            ADD CONSTRAINT chk_employers_status
            CHECK (status IN ('pending', 'verified', 'rejected'));
    END IF;
END $$;
