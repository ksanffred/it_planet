package ru.tramplin_itplanet.tramplin.domain.repository;

import ru.tramplin_itplanet.tramplin.domain.model.Opportunity;

import java.util.Optional;

public interface OpportunityRepository {

    Optional<Opportunity> findById(Long id);
}
