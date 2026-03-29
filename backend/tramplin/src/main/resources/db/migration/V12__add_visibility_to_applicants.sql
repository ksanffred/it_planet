ALTER TABLE applicants
    ADD COLUMN IF NOT EXISTS visibility VARCHAR(20) NOT NULL DEFAULT 'PRIVATE';

ALTER TABLE applicants
    DROP CONSTRAINT IF EXISTS chk_applicants_visibility;

ALTER TABLE applicants
    ADD CONSTRAINT chk_applicants_visibility
        CHECK (visibility IN ('PUBLIC', 'PRIVATE'));
