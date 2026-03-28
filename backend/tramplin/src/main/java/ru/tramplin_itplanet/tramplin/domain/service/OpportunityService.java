package ru.tramplin_itplanet.tramplin.domain.service;

import ru.tramplin_itplanet.tramplin.domain.model.CreateOpportunityCommand;
import ru.tramplin_itplanet.tramplin.domain.model.EmployerOpportunityPosting;
import ru.tramplin_itplanet.tramplin.domain.model.OpportunityMiniCard;
import ru.tramplin_itplanet.tramplin.domain.model.Opportunity;
import ru.tramplin_itplanet.tramplin.domain.model.UpdateOpportunityCommand;

import java.util.List;

public interface OpportunityService {

    List<Opportunity> findAll();

    List<OpportunityMiniCard> findActiveMiniCards(String search);

    List<EmployerOpportunityPosting> findByEmployerId(Long employerId);

    Opportunity getById(Long id);

    Opportunity create(CreateOpportunityCommand command);

    Opportunity update(Long id, UpdateOpportunityCommand command);
}
