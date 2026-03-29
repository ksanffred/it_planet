package ru.tramplin_itplanet.tramplin.di;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tramplin_itplanet.tramplin.datasource.entity.ApplicantEntity;
import ru.tramplin_itplanet.tramplin.datasource.entity.EmployerEntity;
import ru.tramplin_itplanet.tramplin.datasource.entity.OpportunityEntity;
import ru.tramplin_itplanet.tramplin.datasource.entity.OpportunityResponseEntity;
import ru.tramplin_itplanet.tramplin.datasource.entity.TagEntity;
import ru.tramplin_itplanet.tramplin.datasource.entity.UserEntity;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaEmployerRepository;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaApplicantRepository;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaApplicantOpportunityRecommendationRepository;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaOpportunityRepository;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaOpportunityResponseRepository;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaUserRepository;
import ru.tramplin_itplanet.tramplin.domain.exception.ApplicantNotFoundException;
import ru.tramplin_itplanet.tramplin.domain.exception.EmployerNotFoundException;
import ru.tramplin_itplanet.tramplin.domain.exception.OpportunityNotFoundException;
import ru.tramplin_itplanet.tramplin.domain.exception.OpportunityResponseAlreadyExistsException;
import ru.tramplin_itplanet.tramplin.domain.model.ApplicantOpportunityResponseCard;
import ru.tramplin_itplanet.tramplin.domain.model.EmployerOpportunityApplication;
import ru.tramplin_itplanet.tramplin.domain.model.OpportunityResponse;
import ru.tramplin_itplanet.tramplin.domain.model.OpportunityResponseStatus;
import ru.tramplin_itplanet.tramplin.domain.model.Tag;
import ru.tramplin_itplanet.tramplin.domain.model.UserRole;
import ru.tramplin_itplanet.tramplin.domain.service.OpportunityResponseService;

import java.util.Objects;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class OpportunityResponseServiceImpl implements OpportunityResponseService {

    private static final Logger log = LoggerFactory.getLogger(OpportunityResponseServiceImpl.class);

    private final JpaUserRepository jpaUserRepository;
    private final JpaEmployerRepository jpaEmployerRepository;
    private final JpaApplicantRepository jpaApplicantRepository;
    private final JpaOpportunityRepository jpaOpportunityRepository;
    private final JpaOpportunityResponseRepository jpaOpportunityResponseRepository;
    private final JpaApplicantOpportunityRecommendationRepository jpaRecommendationRepository;

    public OpportunityResponseServiceImpl(
            JpaUserRepository jpaUserRepository,
            JpaEmployerRepository jpaEmployerRepository,
            JpaApplicantRepository jpaApplicantRepository,
            JpaOpportunityRepository jpaOpportunityRepository,
            JpaOpportunityResponseRepository jpaOpportunityResponseRepository,
            JpaApplicantOpportunityRecommendationRepository jpaRecommendationRepository
    ) {
        this.jpaUserRepository = jpaUserRepository;
        this.jpaEmployerRepository = jpaEmployerRepository;
        this.jpaApplicantRepository = jpaApplicantRepository;
        this.jpaOpportunityRepository = jpaOpportunityRepository;
        this.jpaOpportunityResponseRepository = jpaOpportunityResponseRepository;
        this.jpaRecommendationRepository = jpaRecommendationRepository;
    }

    @Override
    @Transactional
    public OpportunityResponse apply(Long opportunityId, String userEmail) {
        log.info("Creating opportunity response: opportunityId={}, userEmail={}", opportunityId, userEmail);

        ApplicantEntity applicant = resolveApplicantByUserEmail(userEmail);

        OpportunityEntity opportunity = jpaOpportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new OpportunityNotFoundException(opportunityId));

        if (jpaOpportunityResponseRepository.existsByOpportunity_IdAndApplicant_Id(opportunityId, applicant.getId())) {
            throw new OpportunityResponseAlreadyExistsException(opportunityId, applicant.getId());
        }

        OpportunityResponseEntity entity = new OpportunityResponseEntity();
        entity.setOpportunity(opportunity);
        entity.setApplicant(applicant);
        entity.setStatus(OpportunityResponseStatus.NOT_REVIEWED);

        OpportunityResponseEntity saved = jpaOpportunityResponseRepository.save(entity);
        return new OpportunityResponse(
                saved.getId(),
                saved.getOpportunity().getId(),
                saved.getApplicant().getId(),
                saved.getStatus(),
                saved.getCreatedAt(),
                saved.getUpdatedAt()
        );
    }

    @Override
    public List<ApplicantOpportunityResponseCard> getMyResponses(String userEmail) {
        log.info("Loading my opportunity responses: userEmail={}", userEmail);
        ApplicantEntity applicant = resolveApplicantByUserEmail(userEmail);
        return jpaOpportunityResponseRepository.findAllByApplicantIdWithOpportunity(applicant.getId()).stream()
                .map(response -> new ApplicantOpportunityResponseCard(
                        response.getOpportunity().getTitle(),
                        response.getOpportunity().getEmployer().getName(),
                        response.getStatus(),
                        response.getOpportunity().getType(),
                        response.getOpportunity().getStatus()
                ))
                .toList();
    }

    @Override
    public List<EmployerOpportunityApplication> getApplicationsForOpportunity(Long opportunityId, String userEmail) {
        log.info("Loading applications for opportunityId={}, userEmail={}", opportunityId, userEmail);
        EmployerEntity employer = resolveEmployerByUserEmail(userEmail);

        OpportunityEntity opportunity = jpaOpportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new OpportunityNotFoundException(opportunityId));

        if (!Objects.equals(opportunity.getEmployer().getId(), employer.getId())) {
            throw new AccessDeniedException("You can view only applications for your opportunities");
        }

        List<OpportunityResponseEntity> responses = jpaOpportunityResponseRepository.findAllByOpportunityIdWithDetails(opportunityId);
        Map<Long, Long> recommendationsByApplicant = loadRecommendationCountsByApplicant(opportunityId, responses);

        return responses.stream()
                .map(response -> toEmployerApplication(response, recommendationsByApplicant.getOrDefault(
                        response.getApplicant().getId(),
                        0L
                )))
                .toList();
    }

    @Override
    public List<EmployerOpportunityApplication> getApplicationsForMyOpportunities(String userEmail) {
        log.info("Loading applications for all employer opportunities, userEmail={}", userEmail);
        EmployerEntity employer = resolveEmployerByUserEmail(userEmail);
        List<OpportunityResponseEntity> responses = jpaOpportunityResponseRepository.findAllByEmployerIdWithDetails(employer.getId());
        Map<String, Long> recommendationsByOpportunityAndApplicant =
                loadRecommendationCountsByOpportunityAndApplicant(responses);

        return responses.stream()
                .map(response -> toEmployerApplication(
                        response,
                        recommendationsByOpportunityAndApplicant.getOrDefault(
                                recommendationKey(response.getOpportunity().getId(), response.getApplicant().getId()),
                                0L
                        )
                ))
                .toList();
    }

    private ApplicantEntity resolveApplicantByUserEmail(String userEmail) {
        UserEntity user = jpaUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.warn("Authenticated user not found by email={}", userEmail);
                    return new BadCredentialsException("Invalid authentication token");
                });

        if (user.getRole() != UserRole.APPLICANT) {
            throw new AccessDeniedException("User role must be APPLICANT");
        }

        ApplicantEntity applicant = jpaApplicantRepository.findByUserIdWithSkills(user.getId())
                .orElseThrow(() -> {
                    log.warn("Applicant profile not found for userId={}", user.getId());
                    return new ApplicantNotFoundException(user.getId());
                });
        return applicant;
    }

    private EmployerEntity resolveEmployerByUserEmail(String userEmail) {
        UserEntity user = jpaUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.warn("Authenticated user not found by email={}", userEmail);
                    return new BadCredentialsException("Invalid authentication token");
                });

        if (user.getRole() != UserRole.EMPLOYER) {
            throw new AccessDeniedException("User role must be EMPLOYER");
        }

        return jpaEmployerRepository.findByUserId(user.getId())
                .orElseThrow(() -> {
                    log.warn("Employer profile not found for userId={}", user.getId());
                    return new EmployerNotFoundException(user.getId());
                });
    }

    private Map<Long, Long> loadRecommendationCountsByApplicant(
            Long opportunityId,
            List<OpportunityResponseEntity> responses
    ) {
        List<Long> applicantIds = responses.stream()
                .map(response -> response.getApplicant().getId())
                .distinct()
                .toList();
        if (applicantIds.isEmpty()) {
            return Map.of();
        }

        return jpaRecommendationRepository.countRecommendationsByOpportunityAndApplicants(opportunityId, applicantIds)
                .stream()
                .collect(Collectors.toMap(
                        JpaApplicantOpportunityRecommendationRepository.RecommendationCountProjection::getRecommendedApplicantId,
                        JpaApplicantOpportunityRecommendationRepository.RecommendationCountProjection::getRecommendationsCount,
                        (first, second) -> first
                ));
    }

    private Map<String, Long> loadRecommendationCountsByOpportunityAndApplicant(List<OpportunityResponseEntity> responses) {
        Map<Long, List<OpportunityResponseEntity>> responsesByOpportunity = responses.stream()
                .collect(Collectors.groupingBy(response -> response.getOpportunity().getId()));

        Map<String, Long> result = new java.util.HashMap<>();
        for (Map.Entry<Long, List<OpportunityResponseEntity>> entry : responsesByOpportunity.entrySet()) {
            Long opportunityId = entry.getKey();
            Map<Long, Long> countsByApplicant = loadRecommendationCountsByApplicant(opportunityId, entry.getValue());
            for (Map.Entry<Long, Long> applicantCount : countsByApplicant.entrySet()) {
                result.put(recommendationKey(opportunityId, applicantCount.getKey()), applicantCount.getValue());
            }
        }
        return result;
    }

    private static String recommendationKey(Long opportunityId, Long applicantId) {
        return opportunityId + ":" + applicantId;
    }

    private EmployerOpportunityApplication toEmployerApplication(OpportunityResponseEntity response, long recommendation) {
        OpportunityEntity opportunity = response.getOpportunity();
        ApplicantEntity applicant = response.getApplicant();
        Set<Long> applicantSkillIds = applicant.getSkills().stream()
                .map(TagEntity::getId)
                .collect(Collectors.toSet());

        List<Tag> matchingTags = opportunity.getTags().stream()
                .filter(tag -> applicantSkillIds.contains(tag.getId()))
                .map(tag -> new Tag(tag.getId(), tag.getName(), tag.getCategory()))
                .toList();

        return new EmployerOpportunityApplication(
                applicant.getId(),
                applicant.getName(),
                applicant.getUniversity(),
                applicant.getDesiredPosition(),
                recommendation,
                matchingTags
        );
    }
}
