package ru.tramplin_itplanet.tramplin.datasource.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.tramplin_itplanet.tramplin.datasource.entity.ApplicantOpportunityRecommendationEntity;

public interface JpaApplicantOpportunityRecommendationRepository
        extends JpaRepository<ApplicantOpportunityRecommendationEntity, Long> {

    @Query("SELECT COUNT(r) > 0 FROM ApplicantOpportunityRecommendationEntity r " +
           "WHERE r.recommender.id = :recommenderApplicantId " +
           "AND r.recommendedApplicant.id = :recommendedApplicantId " +
           "AND r.opportunity.id = :opportunityId")
    boolean existsRecommendation(
            @Param("recommenderApplicantId") Long recommenderApplicantId,
            @Param("recommendedApplicantId") Long recommendedApplicantId,
            @Param("opportunityId") Long opportunityId
    );
}
