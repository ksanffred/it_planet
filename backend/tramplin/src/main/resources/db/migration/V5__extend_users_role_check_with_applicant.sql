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
        CHECK (role IN ('USER', 'APPLICANT', 'EMPLOYER', 'ADMIN'));
END $$;
