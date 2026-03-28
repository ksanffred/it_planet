CREATE TABLE IF NOT EXISTS applicant_favorite_opportunities (
    applicant_id   BIGINT    NOT NULL,
    opportunity_id BIGINT    NOT NULL,
    created_at     TIMESTAMP NOT NULL,
    PRIMARY KEY (applicant_id, opportunity_id),
    CONSTRAINT fk_applicant_favorite_opportunities_applicant
        FOREIGN KEY (applicant_id) REFERENCES applicants (id) ON DELETE CASCADE,
    CONSTRAINT fk_applicant_favorite_opportunities_opportunity
        FOREIGN KEY (opportunity_id) REFERENCES opportunities (id) ON DELETE CASCADE
);
