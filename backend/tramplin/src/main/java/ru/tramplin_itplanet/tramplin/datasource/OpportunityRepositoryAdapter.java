package ru.tramplin_itplanet.tramplin.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import ru.tramplin_itplanet.tramplin.datasource.entity.EmployerEntity;
import ru.tramplin_itplanet.tramplin.datasource.entity.OpportunityEntity;
import ru.tramplin_itplanet.tramplin.datasource.entity.TagEntity;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaEmployerRepository;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaOpportunityRepository;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaTagRepository;
import ru.tramplin_itplanet.tramplin.datasource.mapper.OpportunityEntityMapper;
import ru.tramplin_itplanet.tramplin.domain.exception.EmployerNotFoundException;
import ru.tramplin_itplanet.tramplin.domain.model.CreateOpportunityCommand;
import ru.tramplin_itplanet.tramplin.domain.model.Opportunity;
import ru.tramplin_itplanet.tramplin.domain.repository.OpportunityRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class OpportunityRepositoryAdapter implements OpportunityRepository {

    private static final Logger log = LoggerFactory.getLogger(OpportunityRepositoryAdapter.class);

    private final JpaOpportunityRepository jpaOpportunityRepository;
    private final JpaEmployerRepository jpaEmployerRepository;
    private final JpaTagRepository jpaTagRepository;

    public OpportunityRepositoryAdapter(JpaOpportunityRepository jpaOpportunityRepository,
                                        JpaEmployerRepository jpaEmployerRepository,
                                        JpaTagRepository jpaTagRepository) {
        this.jpaOpportunityRepository = jpaOpportunityRepository;
        this.jpaEmployerRepository = jpaEmployerRepository;
        this.jpaTagRepository = jpaTagRepository;
    }

    @Override
    public Optional<Opportunity> findById(Long id) {
        log.debug("Querying database for opportunity with id: {}", id);
        return jpaOpportunityRepository.findByIdWithDetails(id)
                .map(OpportunityEntityMapper::toDomain);
    }

    @Override
    public Opportunity save(CreateOpportunityCommand command) {
        log.debug("Persisting opportunity: title={}, employerId={}", command.title(), command.employerId());

        EmployerEntity employer = jpaEmployerRepository.findById(command.employerId())
                .orElseThrow(() -> {
                    log.warn("Employer not found with id: {}", command.employerId());
                    return new EmployerNotFoundException(command.employerId());
                });

        List<TagEntity> tags = command.tagIds().isEmpty()
                ? List.of()
                : jpaTagRepository.findAllById(command.tagIds());

        OpportunityEntity entity = buildEntity(command, employer, tags);
        OpportunityEntity saved = jpaOpportunityRepository.save(entity);

        log.debug("Opportunity persisted with id: {}", saved.getId());
        return OpportunityEntityMapper.toDomain(saved);
    }

    private OpportunityEntity buildEntity(CreateOpportunityCommand command,
                                          EmployerEntity employer,
                                          List<TagEntity> tags) {
        OpportunityEntity entity = new OpportunityEntity();
        entity.setEmployer(employer);
        entity.setTitle(command.title());
        entity.setDescription(command.description());
        entity.setType(command.type());
        entity.setFormat(command.format());
        entity.setAddress(command.address());
        entity.setCity(command.city());
        entity.setLat(command.lat());
        entity.setLng(command.lng());
        entity.setSalaryFrom(command.salaryFrom());
        entity.setSalaryTo(command.salaryTo());
        entity.setPublishedAt(command.publishedAt() != null ? command.publishedAt() : LocalDateTime.now());
        entity.setExpiresAt(command.expiresAt());
        entity.setStatus(command.status());
        entity.setMedia(command.media());
        entity.setTags(tags);
        return entity;
    }
}
