ALTER TABLE applicants
    ADD COLUMN IF NOT EXISTS desired_position VARCHAR(255);
