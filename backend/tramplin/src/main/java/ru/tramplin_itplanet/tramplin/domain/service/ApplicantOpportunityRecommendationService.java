package ru.tramplin_itplanet.tramplin.domain.service;

import ru.tramplin_itplanet.tramplin.domain.model.ApplicantOpportunityRecommendation;

public interface ApplicantOpportunityRecommendationService {
    ApplicantOpportunityRecommendation create(String userEmail, Long opportunityId, Long recommendedApplicantId);
}
