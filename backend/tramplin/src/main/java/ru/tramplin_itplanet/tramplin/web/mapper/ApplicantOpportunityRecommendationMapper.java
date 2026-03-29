package ru.tramplin_itplanet.tramplin.web.mapper;

import ru.tramplin_itplanet.tramplin.domain.model.ApplicantOpportunityRecommendation;
import ru.tramplin_itplanet.tramplin.web.dto.ApplicantOpportunityRecommendationResponse;

public final class ApplicantOpportunityRecommendationMapper {

    private ApplicantOpportunityRecommendationMapper() {
    }

    public static ApplicantOpportunityRecommendationResponse toResponse(
            ApplicantOpportunityRecommendation recommendation
    ) {
        return new ApplicantOpportunityRecommendationResponse(
                recommendation.id(),
                recommendation.recommenderApplicantId(),
                recommendation.recommendedApplicantId(),
                recommendation.opportunityId(),
                recommendation.createdAt()
        );
    }
}
