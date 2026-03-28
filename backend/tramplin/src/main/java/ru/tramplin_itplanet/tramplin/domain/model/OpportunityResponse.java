package ru.tramplin_itplanet.tramplin.domain.model;

import java.time.LocalDateTime;

public record OpportunityResponse(
        Long id,
        Long opportunityId,
        Long applicantId,
        OpportunityResponseStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
