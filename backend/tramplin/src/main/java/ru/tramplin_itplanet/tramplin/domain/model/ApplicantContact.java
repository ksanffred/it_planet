package ru.tramplin_itplanet.tramplin.domain.model;

import java.time.LocalDateTime;

public record ApplicantContact(
        Long id,
        Long requesterApplicantId,
        Long recipientApplicantId,
        ApplicantContactStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
