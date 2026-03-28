CREATE TABLE IF NOT EXISTS opportunity_responses (
    id             BIGSERIAL PRIMARY KEY,
    opportunity_id BIGINT       NOT NULL,
    applicant_id   BIGINT       NOT NULL,
    status         VARCHAR(50)  NOT NULL,
    created_at     TIMESTAMP    NOT NULL,
    updated_at     TIMESTAMP    NOT NULL,
    CONSTRAINT uq_opportunity_responses_opportunity_applicant
        UNIQUE (opportunity_id, applicant_id),
    CONSTRAINT fk_opportunity_responses_opportunity
        FOREIGN KEY (opportunity_id) REFERENCES opportunities (id) ON DELETE CASCADE,
    CONSTRAINT fk_opportunity_responses_applicant
        FOREIGN KEY (applicant_id) REFERENCES applicants (id) ON DELETE CASCADE,
    CONSTRAINT chk_opportunity_responses_status
        CHECK (status IN ('NOT_REVIEWED', 'ON_HOLD', 'REJECTED', 'ACCEPTED'))
);
