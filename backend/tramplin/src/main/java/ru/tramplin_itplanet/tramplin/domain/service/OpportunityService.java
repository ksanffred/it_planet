package ru.tramplin_itplanet.tramplin.domain.service;

import ru.tramplin_itplanet.tramplin.domain.model.CreateOpportunityCommand;
import ru.tramplin_itplanet.tramplin.domain.model.OpportunityMiniCard;
import ru.tramplin_itplanet.tramplin.domain.model.Opportunity;

import java.util.List;

public interface OpportunityService {

    List<Opportunity> findAll();

    List<OpportunityMiniCard> findActiveMiniCards();

    Opportunity getById(Long id);

    Opportunity create(CreateOpportunityCommand command);
}
