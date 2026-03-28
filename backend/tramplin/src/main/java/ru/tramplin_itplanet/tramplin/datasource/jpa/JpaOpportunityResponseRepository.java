package ru.tramplin_itplanet.tramplin.datasource.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.tramplin_itplanet.tramplin.datasource.entity.OpportunityResponseEntity;

public interface JpaOpportunityResponseRepository extends JpaRepository<OpportunityResponseEntity, Long> {
    boolean existsByOpportunity_IdAndApplicant_Id(Long opportunityId, Long applicantId);
}
