CREATE TABLE IF NOT EXISTS applicant_opportunity_recommendations (
    id                     BIGSERIAL PRIMARY KEY,
    recommender_id         BIGINT    NOT NULL,
    recommended_applicant_id BIGINT  NOT NULL,
    opportunity_id         BIGINT    NOT NULL,
    created_at             TIMESTAMP NOT NULL,
    CONSTRAINT uq_applicant_opportunity_recommendations
        UNIQUE (recommender_id, recommended_applicant_id, opportunity_id),
    CONSTRAINT chk_applicant_opportunity_recommendations_not_self
        CHECK (recommender_id <> recommended_applicant_id),
    CONSTRAINT fk_applicant_opportunity_recommendations_recommender
        FOREIGN KEY (recommender_id) REFERENCES applicants (id) ON DELETE CASCADE,
    CONSTRAINT fk_applicant_opportunity_recommendations_recommended
        FOREIGN KEY (recommended_applicant_id) REFERENCES applicants (id) ON DELETE CASCADE,
    CONSTRAINT fk_applicant_opportunity_recommendations_opportunity
        FOREIGN KEY (opportunity_id) REFERENCES opportunities (id) ON DELETE CASCADE
);
