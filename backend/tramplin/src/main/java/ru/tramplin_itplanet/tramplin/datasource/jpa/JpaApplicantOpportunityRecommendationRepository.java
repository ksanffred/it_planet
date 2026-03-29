package ru.tramplin_itplanet.tramplin.datasource.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.tramplin_itplanet.tramplin.datasource.entity.ApplicantOpportunityRecommendationEntity;

import java.util.List;

public interface JpaApplicantOpportunityRecommendationRepository
        extends JpaRepository<ApplicantOpportunityRecommendationEntity, Long> {

    interface RecommendationCountProjection {
        Long getRecommendedApplicantId();
        long getRecommendationsCount();
    }

    @Query("SELECT COUNT(r) > 0 FROM ApplicantOpportunityRecommendationEntity r " +
           "WHERE r.recommender.id = :recommenderApplicantId " +
           "AND r.recommendedApplicant.id = :recommendedApplicantId " +
           "AND r.opportunity.id = :opportunityId")
    boolean existsRecommendation(
            @Param("recommenderApplicantId") Long recommenderApplicantId,
            @Param("recommendedApplicantId") Long recommendedApplicantId,
            @Param("opportunityId") Long opportunityId
    );

    @Query("SELECT r.recommendedApplicant.id AS recommendedApplicantId, COUNT(r.id) AS recommendationsCount " +
           "FROM ApplicantOpportunityRecommendationEntity r " +
           "WHERE r.opportunity.id = :opportunityId " +
           "AND r.recommendedApplicant.id IN :applicantIds " +
           "GROUP BY r.recommendedApplicant.id")
    List<RecommendationCountProjection> countRecommendationsByOpportunityAndApplicants(
            @Param("opportunityId") Long opportunityId,
            @Param("applicantIds") List<Long> applicantIds
    );
}
