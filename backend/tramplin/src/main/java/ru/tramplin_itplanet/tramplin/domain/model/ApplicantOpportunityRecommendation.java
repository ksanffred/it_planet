package ru.tramplin_itplanet.tramplin.domain.model;

import java.time.LocalDateTime;

public record ApplicantOpportunityRecommendation(
        Long id,
        Long recommenderApplicantId,
        Long recommendedApplicantId,
        Long opportunityId,
        LocalDateTime createdAt
) {}
