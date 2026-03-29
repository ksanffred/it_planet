package ru.tramplin_itplanet.tramplin.domain.exception;

public class ApplicantOpportunityRecommendationAlreadyExistsException extends RuntimeException {

    public ApplicantOpportunityRecommendationAlreadyExistsException(
            Long recommenderApplicantId,
            Long recommendedApplicantId,
            Long opportunityId
    ) {
        super("Recommendation already exists for recommender id: " + recommenderApplicantId
                + ", recommended applicant id: " + recommendedApplicantId
                + ", opportunity id: " + opportunityId);
    }
}
