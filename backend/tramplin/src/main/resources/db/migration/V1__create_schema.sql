CREATE TABLE IF NOT EXISTS tags (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL UNIQUE,
    category   VARCHAR(50)  NOT NULL
);

CREATE TABLE IF NOT EXISTS employers (
    id             BIGSERIAL PRIMARY KEY,
    user_id        BIGINT,
    company_name   VARCHAR(255) NOT NULL,
    description    TEXT,
    inn            VARCHAR(20),
    website        VARCHAR(255),
    socials        TEXT,
    logo_url       VARCHAR(255),
    status         VARCHAR(50)  NOT NULL,
    CONSTRAINT chk_employers_status
        CHECK (status IN ('pending', 'verified', 'rejected'))
);

CREATE TABLE IF NOT EXISTS opportunities (
    id           BIGSERIAL PRIMARY KEY,
    employer_id  BIGINT       NOT NULL,
    title        VARCHAR(255) NOT NULL,
    description  TEXT,
    type         VARCHAR(50)  NOT NULL,
    format       VARCHAR(50)  NOT NULL,
    address      VARCHAR(255),
    city         VARCHAR(255),
    lat          DOUBLE PRECISION,
    lng          DOUBLE PRECISION,
    salary_from  DECIMAL(10, 2),
    salary_to    DECIMAL(10, 2),
    published_at TIMESTAMP,
    expires_at   TIMESTAMP,
    status       VARCHAR(50)  NOT NULL,

    CONSTRAINT fk_opportunity_employer
        FOREIGN KEY (employer_id) REFERENCES employers (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS opportunity_media (
    opportunity_id BIGINT       NOT NULL,
    media_url      VARCHAR(500),

    CONSTRAINT fk_media_opportunity
        FOREIGN KEY (opportunity_id) REFERENCES opportunities (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS opportunity_tags (
    opportunity_id BIGINT NOT NULL,
    tag_id         BIGINT NOT NULL,

    PRIMARY KEY (opportunity_id, tag_id),
    CONSTRAINT fk_optag_opportunity FOREIGN KEY (opportunity_id) REFERENCES opportunities (id) ON DELETE CASCADE,
    CONSTRAINT fk_optag_tag         FOREIGN KEY (tag_id)         REFERENCES tags (id)          ON DELETE CASCADE
);
