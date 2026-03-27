package ru.tramplin_itplanet.tramplin.domain.repository;

import ru.tramplin_itplanet.tramplin.domain.model.CreateOpportunityCommand;
import ru.tramplin_itplanet.tramplin.domain.model.Opportunity;

import java.util.List;
import java.util.Optional;

public interface OpportunityRepository {

    List<Opportunity> findAll();

    Optional<Opportunity> findById(Long id);

    Opportunity save(CreateOpportunityCommand command);
}
