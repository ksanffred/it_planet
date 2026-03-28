package ru.tramplin_itplanet.tramplin.domain.repository;

import ru.tramplin_itplanet.tramplin.domain.model.CreateOpportunityCommand;
import ru.tramplin_itplanet.tramplin.domain.model.OpportunityMiniCard;
import ru.tramplin_itplanet.tramplin.domain.model.Opportunity;
import ru.tramplin_itplanet.tramplin.domain.model.EmployerOpportunityPosting;
import ru.tramplin_itplanet.tramplin.domain.model.UpdateOpportunityCommand;

import java.util.List;
import java.util.Optional;

public interface OpportunityRepository {

    List<Opportunity> findAll();

    List<OpportunityMiniCard> findActiveMiniCards(String search);

    List<EmployerOpportunityPosting> findByEmployerId(Long employerId);

    Optional<Opportunity> findById(Long id);

    Opportunity save(CreateOpportunityCommand command);

    Opportunity update(Long id, UpdateOpportunityCommand command);
}
