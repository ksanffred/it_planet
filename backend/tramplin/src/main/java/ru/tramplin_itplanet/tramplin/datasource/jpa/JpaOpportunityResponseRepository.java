package ru.tramplin_itplanet.tramplin.datasource.jpa;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.tramplin_itplanet.tramplin.datasource.entity.OpportunityResponseEntity;

import java.util.List;

public interface JpaOpportunityResponseRepository extends JpaRepository<OpportunityResponseEntity, Long> {
    boolean existsByOpportunity_IdAndApplicant_Id(Long opportunityId, Long applicantId);

    @Query("SELECT r FROM OpportunityResponseEntity r " +
           "JOIN FETCH r.opportunity o " +
           "JOIN FETCH o.employer " +
           "WHERE r.applicant.id = :applicantId " +
           "ORDER BY r.createdAt DESC, r.id DESC")
    List<OpportunityResponseEntity> findAllByApplicantIdWithOpportunity(@Param("applicantId") Long applicantId);
}
