CREATE TABLE IF NOT EXISTS applicant_contacts (
    id           BIGSERIAL PRIMARY KEY,
    requester_id BIGINT      NOT NULL,
    recipient_id BIGINT      NOT NULL,
    status       VARCHAR(50) NOT NULL,
    created_at   TIMESTAMP   NOT NULL,
    updated_at   TIMESTAMP   NOT NULL,
    CONSTRAINT uq_applicant_contacts_requester_recipient
        UNIQUE (requester_id, recipient_id),
    CONSTRAINT chk_applicant_contacts_status
        CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED')),
    CONSTRAINT chk_applicant_contacts_not_self
        CHECK (requester_id <> recipient_id),
    CONSTRAINT fk_applicant_contacts_requester
        FOREIGN KEY (requester_id) REFERENCES applicants (id) ON DELETE CASCADE,
    CONSTRAINT fk_applicant_contacts_recipient
        FOREIGN KEY (recipient_id) REFERENCES applicants (id) ON DELETE CASCADE
);
