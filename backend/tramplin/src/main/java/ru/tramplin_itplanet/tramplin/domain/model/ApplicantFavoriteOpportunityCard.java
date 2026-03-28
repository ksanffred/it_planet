package ru.tramplin_itplanet.tramplin.domain.model;

public record ApplicantFavoriteOpportunityCard(
        String title,
        String companyName,
        OpportunityStatus status,
        OpportunityType type
) {}
