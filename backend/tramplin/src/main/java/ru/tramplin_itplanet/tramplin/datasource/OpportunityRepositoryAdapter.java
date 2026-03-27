package ru.tramplin_itplanet.tramplin.datasource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import ru.tramplin_itplanet.tramplin.datasource.entity.EmployerEntity;
import ru.tramplin_itplanet.tramplin.datasource.entity.OpportunityEntity;
import ru.tramplin_itplanet.tramplin.datasource.entity.TagEntity;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaEmployerRepository;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaOpportunityRepository;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaTagRepository;
import ru.tramplin_itplanet.tramplin.datasource.mapper.OpportunityEntityMapper;
import ru.tramplin_itplanet.tramplin.domain.exception.EmployerNotFoundException;
import ru.tramplin_itplanet.tramplin.domain.model.CreateOpportunityCommand;
import ru.tramplin_itplanet.tramplin.domain.model.OpportunityMiniCard;
import ru.tramplin_itplanet.tramplin.domain.model.Opportunity;
import ru.tramplin_itplanet.tramplin.domain.model.OpportunityStatus;
import ru.tramplin_itplanet.tramplin.domain.repository.OpportunityRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Repository
public class OpportunityRepositoryAdapter implements OpportunityRepository {

    private static final Logger log = LoggerFactory.getLogger(OpportunityRepositoryAdapter.class);
    private static final String MINI_CARD_KEY_PREFIX = "app:short:";

    private final JpaOpportunityRepository jpaOpportunityRepository;
    private final JpaEmployerRepository jpaEmployerRepository;
    private final JpaTagRepository jpaTagRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${app.cache.feed-ttl-minutes:12}")
    private long feedTtlMinutes;

    public OpportunityRepositoryAdapter(JpaOpportunityRepository jpaOpportunityRepository,
                                        JpaEmployerRepository jpaEmployerRepository,
                                        JpaTagRepository jpaTagRepository,
                                        StringRedisTemplate redisTemplate,
                                        ObjectMapper objectMapper) {
        this.jpaOpportunityRepository = jpaOpportunityRepository;
        this.jpaEmployerRepository = jpaEmployerRepository;
        this.jpaTagRepository = jpaTagRepository;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<Opportunity> findAll() {
        log.debug("Querying database for all opportunities");
        return jpaOpportunityRepository.findAllWithDetailsOrderByPublishedAtDesc()
                .stream()
                .map(OpportunityEntityMapper::toDomain)
                .toList();
    }

    @Override
    public List<OpportunityMiniCard> findActiveMiniCards(String search) {
        List<Long> activeIds = resolveActiveIds(search);
        if (activeIds.isEmpty()) {
            return List.of();
        }

        List<String> keys = activeIds.stream().map(this::miniCardKey).toList();
        List<String> cachedPayloads = redisTemplate.opsForValue().multiGet(keys);

        Map<Long, OpportunityMiniCard> byId = new HashMap<>();
        List<Long> missedIds = new ArrayList<>();

        for (int i = 0; i < activeIds.size(); i++) {
            Long id = activeIds.get(i);
            String payload = cachedPayloads == null ? null : cachedPayloads.get(i);
            OpportunityMiniCard cached = deserializeMiniCard(payload, id);
            if (cached == null) {
                missedIds.add(id);
                continue;
            }
            byId.put(id, cached);
        }

        if (!missedIds.isEmpty()) {
            List<OpportunityMiniCard> loaded = loadMiniCardsFromDatabase(missedIds);
            cacheMiniCards(loaded);
            loaded.forEach(card -> byId.put(card.id(), card));
        }

        return activeIds.stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public Optional<Opportunity> findById(Long id) {
        log.debug("Querying database for opportunity with id: {}", id);
        return jpaOpportunityRepository.findByIdWithDetails(id)
                .map(OpportunityEntityMapper::toDomain);
    }

    @Override
    @CacheEvict(value = "opportunity-feed", allEntries = true)
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

    private List<OpportunityMiniCard> loadMiniCardsFromDatabase(List<Long> ids) {
        Map<Long, OpportunityMiniCard> byId = jpaOpportunityRepository.findAllByIdInAndStatusWithDetails(ids, OpportunityStatus.ACTIVE)
                .stream()
                .map(this::toMiniCard)
                .collect(HashMap::new, (map, card) -> map.put(card.id(), card), HashMap::putAll);

        return ids.stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .toList();
    }

    private void cacheMiniCards(List<OpportunityMiniCard> cards) {
        for (OpportunityMiniCard card : cards) {
            try {
                String payload = objectMapper.writeValueAsString(card);
                redisTemplate.opsForValue().set(
                        miniCardKey(card.id()),
                        payload,
                        Duration.ofMinutes(feedTtlMinutes)
                );
            } catch (JsonProcessingException e) {
                log.warn("Failed to serialize mini-card for cache, opportunityId={}", card.id());
            }
        }
    }

    private OpportunityMiniCard deserializeMiniCard(String payload, Long id) {
        if (payload == null) {
            return null;
        }
        try {
            return objectMapper.readValue(payload, OpportunityMiniCard.class);
        } catch (JsonProcessingException e) {
            log.warn("Failed to deserialize mini-card from cache, opportunityId={}", id);
            redisTemplate.delete(miniCardKey(id));
            return null;
        }
    }

    private OpportunityMiniCard toMiniCard(OpportunityEntity entity) {
        return new OpportunityMiniCard(
                entity.getId(),
                entity.getMedia().isEmpty() ? null : entity.getMedia().getFirst(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getEmployer().getName(),
                entity.getFormat().name(),
                entity.getTags().stream().limit(3).map(TagEntity::getName).toList()
        );
    }

    private String miniCardKey(Long id) {
        return MINI_CARD_KEY_PREFIX + id;
    }

    private List<Long> resolveActiveIds(String search) {
        if (!StringUtils.hasText(search)) {
            return jpaOpportunityRepository.findIdsByStatusOrderByPublishedAtDesc(OpportunityStatus.ACTIVE);
        }

        String normalizedSearch = search.trim();
        log.debug("Searching active opportunities with full-text query: {}", normalizedSearch);
        return jpaOpportunityRepository.findIdsByStatusAndSearchOrderByPublishedAtDesc(
                OpportunityStatus.ACTIVE.name(),
                normalizedSearch
        );
    }
}
