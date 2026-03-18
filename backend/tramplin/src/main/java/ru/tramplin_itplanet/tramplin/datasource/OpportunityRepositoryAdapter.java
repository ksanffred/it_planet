package ru.tramplin_itplanet.tramplin.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaOpportunityRepository;
import ru.tramplin_itplanet.tramplin.datasource.mapper.OpportunityEntityMapper;
import ru.tramplin_itplanet.tramplin.domain.model.Opportunity;
import ru.tramplin_itplanet.tramplin.domain.repository.OpportunityRepository;

import java.util.Optional;

@Repository
public class OpportunityRepositoryAdapter implements OpportunityRepository {

    private static final Logger log = LoggerFactory.getLogger(OpportunityRepositoryAdapter.class);

    private final JpaOpportunityRepository jpaRepository;

    public OpportunityRepositoryAdapter(JpaOpportunityRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Opportunity> findById(Long id) {
        log.debug("Querying database for opportunity with id: {}", id);
        return jpaRepository.findByIdWithDetails(id)
                .map(OpportunityEntityMapper::toDomain);
    }
}
