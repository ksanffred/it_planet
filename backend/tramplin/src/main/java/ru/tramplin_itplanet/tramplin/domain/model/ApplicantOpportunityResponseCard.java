package ru.tramplin_itplanet.tramplin.domain.model;

public record ApplicantOpportunityResponseCard(
        String title,
        String companyName,
        OpportunityResponseStatus responseStatus,
        OpportunityType opportunityType,
        OpportunityStatus opportunityStatus
) {}
