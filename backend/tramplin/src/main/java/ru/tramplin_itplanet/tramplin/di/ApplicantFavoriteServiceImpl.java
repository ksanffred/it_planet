package ru.tramplin_itplanet.tramplin.di;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tramplin_itplanet.tramplin.datasource.entity.ApplicantEntity;
import ru.tramplin_itplanet.tramplin.datasource.entity.ApplicantFavoriteOpportunityEntity;
import ru.tramplin_itplanet.tramplin.datasource.entity.ApplicantFavoriteOpportunityId;
import ru.tramplin_itplanet.tramplin.datasource.entity.OpportunityEntity;
import ru.tramplin_itplanet.tramplin.datasource.entity.UserEntity;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaApplicantFavoriteOpportunityRepository;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaApplicantRepository;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaOpportunityRepository;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaUserRepository;
import ru.tramplin_itplanet.tramplin.domain.exception.ApplicantNotFoundException;
import ru.tramplin_itplanet.tramplin.domain.exception.OpportunityNotFoundException;
import ru.tramplin_itplanet.tramplin.domain.model.ApplicantFavorites;
import ru.tramplin_itplanet.tramplin.domain.model.ApplicantFavoriteOpportunityCard;
import ru.tramplin_itplanet.tramplin.domain.model.ApplicantVisibility;
import ru.tramplin_itplanet.tramplin.domain.model.UserRole;
import ru.tramplin_itplanet.tramplin.domain.service.ApplicantFavoriteService;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ApplicantFavoriteServiceImpl implements ApplicantFavoriteService {

    private static final Logger log = LoggerFactory.getLogger(ApplicantFavoriteServiceImpl.class);

    private final JpaUserRepository jpaUserRepository;
    private final JpaApplicantRepository jpaApplicantRepository;
    private final JpaOpportunityRepository jpaOpportunityRepository;
    private final JpaApplicantFavoriteOpportunityRepository jpaApplicantFavoriteOpportunityRepository;

    public ApplicantFavoriteServiceImpl(
            JpaUserRepository jpaUserRepository,
            JpaApplicantRepository jpaApplicantRepository,
            JpaOpportunityRepository jpaOpportunityRepository,
            JpaApplicantFavoriteOpportunityRepository jpaApplicantFavoriteOpportunityRepository
    ) {
        this.jpaUserRepository = jpaUserRepository;
        this.jpaApplicantRepository = jpaApplicantRepository;
        this.jpaOpportunityRepository = jpaOpportunityRepository;
        this.jpaApplicantFavoriteOpportunityRepository = jpaApplicantFavoriteOpportunityRepository;
    }

    @Override
    @Transactional
    public ApplicantFavorites addOneByUserEmail(String email, Long opportunityId) {
        log.info("Adding one favorite for email={}, opportunityId={}", email, opportunityId);
        return addManyByUserEmail(email, List.of(opportunityId));
    }

    @Override
    @Transactional
    public ApplicantFavorites removeOneByUserEmail(String email, Long opportunityId) {
        log.info("Removing one favorite for email={}, opportunityId={}", email, opportunityId);

        ApplicantEntity applicant = resolveApplicantByUserEmail(email);

        jpaApplicantFavoriteOpportunityRepository.deleteById(
                new ApplicantFavoriteOpportunityId(applicant.getId(), opportunityId)
        );

        return currentFavorites(applicant.getId());
    }

    @Override
    public List<ApplicantFavoriteOpportunityCard> getCardsByUserEmail(String email) {
        log.info("Loading favorite opportunity cards for email={}", email);
        ApplicantEntity applicant = resolveApplicantByUserEmail(email);
        return jpaApplicantFavoriteOpportunityRepository.findAllByApplicantIdWithOpportunity(applicant.getId()).stream()
                .map(this::toFavoriteCard)
                .toList();
    }

    @Override
    public List<ApplicantFavoriteOpportunityCard> getCardsByApplicantIdForViewer(String viewerEmail, Long applicantId) {
        log.info("Loading favorite opportunity cards by applicant id={}, viewerEmail={}", applicantId, viewerEmail);
        UserEntity viewer = resolveAuthenticatedUserByEmail(viewerEmail);
        ApplicantEntity targetApplicant = jpaApplicantRepository.findById(applicantId)
                .orElseThrow(() -> new ApplicantNotFoundException(applicantId));

        if (effectiveVisibility(targetApplicant) == ApplicantVisibility.PRIVATE
                && canViewPrivateFavorites(viewer, targetApplicant) == false) {
            throw new AccessDeniedException("Favorites are private for this applicant");
        }

        return jpaApplicantFavoriteOpportunityRepository.findAllByApplicantIdWithOpportunity(applicantId).stream()
                .map(this::toFavoriteCard)
                .toList();
    }

    @Override
    @Transactional
    public ApplicantFavorites addManyByUserEmail(String email, List<Long> opportunityIds) {
        int requestedCount = opportunityIds != null ? opportunityIds.size() : 0;
        log.info("Adding many favorites for email={}, requestedCount={}", email, requestedCount);

        UserEntity user = resolveAuthenticatedUserByEmail(email);
        ensureApplicantRole(user);

        ApplicantEntity applicant = jpaApplicantRepository.findByUserIdWithSkills(user.getId())
                .orElseThrow(() -> {
                    log.warn("Applicant profile not found for userId={}", user.getId());
                    return new ApplicantNotFoundException(user.getId());
                });

        List<Long> normalizedOpportunityIds = normalizeOpportunityIds(opportunityIds);
        if (normalizedOpportunityIds.isEmpty()) {
            log.info("No opportunity ids provided after normalization for email={}", email);
            return currentFavorites(applicant.getId());
        }

        List<OpportunityEntity> foundOpportunities = jpaOpportunityRepository.findAllById(normalizedOpportunityIds);
        Map<Long, OpportunityEntity> opportunityById = foundOpportunities.stream()
                .collect(Collectors.toMap(OpportunityEntity::getId, Function.identity()));

        for (Long requestedId : normalizedOpportunityIds) {
            if (opportunityById.containsKey(requestedId) == false) {
                log.warn("Opportunity not found when adding favorites: id={}", requestedId);
                throw new OpportunityNotFoundException(requestedId);
            }
        }

        Set<Long> existingFavoriteIds = new LinkedHashSet<>(
                jpaApplicantFavoriteOpportunityRepository.findOpportunityIdsByApplicantId(applicant.getId())
        );

        List<ApplicantFavoriteOpportunityEntity> newFavorites = normalizedOpportunityIds.stream()
                .filter(opportunityId -> existingFavoriteIds.contains(opportunityId) == false)
                .map(opportunityId -> toFavoriteEntity(applicant, opportunityById.get(opportunityId)))
                .toList();

        if (newFavorites.isEmpty() == false) {
            log.info(
                    "Persisting new favorites for applicantId={}, newCount={}",
                    applicant.getId(),
                    newFavorites.size()
            );
            jpaApplicantFavoriteOpportunityRepository.saveAll(newFavorites);
        }

        return currentFavorites(applicant.getId());
    }

    private UserEntity resolveAuthenticatedUserByEmail(String email) {
        return jpaUserRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("Authenticated user not found by email={}", email);
                    return new BadCredentialsException("Invalid authentication token");
                });
    }

    private static void ensureApplicantRole(UserEntity user) {
        if (user.getRole() != UserRole.APPLICANT) {
            throw new AccessDeniedException("User role must be APPLICANT");
        }
    }

    private ApplicantEntity resolveApplicantByUserEmail(String email) {
        UserEntity user = resolveAuthenticatedUserByEmail(email);
        ensureApplicantRole(user);
        return jpaApplicantRepository.findByUserIdWithSkills(user.getId())
                .orElseThrow(() -> {
                    log.warn("Applicant profile not found for userId={}", user.getId());
                    return new ApplicantNotFoundException(user.getId());
                });
    }

    private static boolean canViewPrivateFavorites(UserEntity viewer, ApplicantEntity targetApplicant) {
        if (viewer.getRole() == UserRole.EMPLOYER || viewer.getRole() == UserRole.CURATOR) {
            return true;
        }
        if (viewer.getRole() == UserRole.APPLICANT) {
            return java.util.Objects.equals(targetApplicant.getUserId(), viewer.getId());
        }
        return false;
    }

    private static ApplicantVisibility effectiveVisibility(ApplicantEntity applicant) {
        return applicant.getVisibility() != null ? applicant.getVisibility() : ApplicantVisibility.PRIVATE;
    }

    private ApplicantFavorites currentFavorites(Long applicantId) {
        List<Long> ids = jpaApplicantFavoriteOpportunityRepository.findOpportunityIdsByApplicantId(applicantId);
        return new ApplicantFavorites(applicantId, ids);
    }

    private static List<Long> normalizeOpportunityIds(List<Long> opportunityIds) {
        if (opportunityIds == null || opportunityIds.isEmpty()) {
            return List.of();
        }
        return opportunityIds.stream()
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.collectingAndThen(
                        Collectors.toCollection(LinkedHashSet::new),
                        List::copyOf
                ));
    }

    private static ApplicantFavoriteOpportunityEntity toFavoriteEntity(
            ApplicantEntity applicant,
            OpportunityEntity opportunity
    ) {
        ApplicantFavoriteOpportunityEntity entity = new ApplicantFavoriteOpportunityEntity();
        entity.setId(new ApplicantFavoriteOpportunityId(applicant.getId(), opportunity.getId()));
        entity.setApplicant(applicant);
        entity.setOpportunity(opportunity);
        entity.setCreatedAt(LocalDateTime.now());
        return entity;
    }

    private ApplicantFavoriteOpportunityCard toFavoriteCard(ApplicantFavoriteOpportunityEntity favorite) {
        OpportunityEntity opportunity = favorite.getOpportunity();
        return new ApplicantFavoriteOpportunityCard(
                opportunity.getTitle(),
                opportunity.getEmployer().getName(),
                opportunity.getStatus(),
                opportunity.getType()
        );
    }
}
