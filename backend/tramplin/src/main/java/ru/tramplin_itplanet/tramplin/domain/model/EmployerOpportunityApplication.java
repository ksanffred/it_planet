package ru.tramplin_itplanet.tramplin.domain.model;

import java.time.LocalDateTime;

public record EmployerOpportunityApplication(
        Long responseId,
        Long opportunityId,
        String title,
        String companyName,
        OpportunityType opportunityType,
        OpportunityStatus opportunityStatus,
        Long applicantId,
        String applicantName,
        OpportunityResponseStatus responseStatus,
        LocalDateTime appliedAt
) {}
