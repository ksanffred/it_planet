CREATE TABLE IF NOT EXISTS applicants (
    id                             BIGSERIAL PRIMARY KEY,
    user_id                        BIGINT       NOT NULL UNIQUE,
    name                           VARCHAR(255) NOT NULL,
    university                     VARCHAR(255),
    faculty                        VARCHAR(255),
    current_field_of_study         VARCHAR(255),
    major                          VARCHAR(255),
    graduation_year                INTEGER,
    additional_education_details   TEXT,
    portfolio_url                  TEXT,
    resume_url                     TEXT,
    CONSTRAINT fk_applicants_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS applicant_skills (
    applicant_id BIGINT NOT NULL,
    tag_id       BIGINT NOT NULL,
    PRIMARY KEY (applicant_id, tag_id),
    CONSTRAINT fk_applicant_skills_applicant
        FOREIGN KEY (applicant_id) REFERENCES applicants (id) ON DELETE CASCADE,
    CONSTRAINT fk_applicant_skills_tag
        FOREIGN KEY (tag_id) REFERENCES tags (id) ON DELETE CASCADE
);
