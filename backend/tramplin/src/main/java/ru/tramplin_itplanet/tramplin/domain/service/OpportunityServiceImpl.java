package ru.tramplin_itplanet.tramplin.domain.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tramplin_itplanet.tramplin.domain.exception.OpportunityNotFoundException;
import ru.tramplin_itplanet.tramplin.domain.model.CreateOpportunityCommand;
import ru.tramplin_itplanet.tramplin.domain.model.Opportunity;
import ru.tramplin_itplanet.tramplin.domain.repository.OpportunityRepository;

@Service
@Transactional(readOnly = true)
public class OpportunityServiceImpl implements OpportunityService {

    private static final Logger log = LoggerFactory.getLogger(OpportunityServiceImpl.class);

    private final OpportunityRepository opportunityRepository;

    public OpportunityServiceImpl(OpportunityRepository opportunityRepository) {
        this.opportunityRepository = opportunityRepository;
    }

    @Override
    public Opportunity getById(Long id) {
        log.info("Fetching opportunity with id: {}", id);
        return opportunityRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Opportunity not found with id: {}", id);
                    return new OpportunityNotFoundException(id);
                });
    }

    @Override
    @Transactional
    public Opportunity create(CreateOpportunityCommand command) {
        log.info("Creating opportunity: title={}, type={}, employerId={}", command.title(), command.type(), command.employerId());
        Opportunity saved = opportunityRepository.save(command);
        log.info("Opportunity created with id: {}", saved.id());
        return saved;
    }
}
