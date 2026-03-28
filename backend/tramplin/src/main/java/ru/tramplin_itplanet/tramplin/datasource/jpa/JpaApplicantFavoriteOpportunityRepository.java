package ru.tramplin_itplanet.tramplin.datasource.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.tramplin_itplanet.tramplin.datasource.entity.ApplicantFavoriteOpportunityEntity;
import ru.tramplin_itplanet.tramplin.datasource.entity.ApplicantFavoriteOpportunityId;

import java.util.List;

public interface JpaApplicantFavoriteOpportunityRepository
        extends JpaRepository<ApplicantFavoriteOpportunityEntity, ApplicantFavoriteOpportunityId> {

    @Query("SELECT f.id.opportunityId FROM ApplicantFavoriteOpportunityEntity f " +
           "WHERE f.id.applicantId = :applicantId " +
           "ORDER BY f.createdAt DESC, f.id.opportunityId DESC")
    List<Long> findOpportunityIdsByApplicantId(@Param("applicantId") Long applicantId);
}
