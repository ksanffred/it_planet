package ru.tramplin_itplanet.tramplin.domain.model;

import java.time.LocalDateTime;

public record EmployerOpportunityPosting(
        Long id,
        String title,
        OpportunityStatus status,
        OpportunityType type,
        LocalDateTime publishedAt,
        LocalDateTime expiresAt,
        long applicationsCount
) {}
