package ru.tramplin_itplanet.tramplin.di;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tramplin_itplanet.tramplin.datasource.entity.ApplicantEntity;
import ru.tramplin_itplanet.tramplin.datasource.entity.ApplicantOpportunityRecommendationEntity;
import ru.tramplin_itplanet.tramplin.datasource.entity.OpportunityEntity;
import ru.tramplin_itplanet.tramplin.datasource.entity.UserEntity;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaApplicantContactRepository;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaApplicantOpportunityRecommendationRepository;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaApplicantRepository;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaOpportunityRepository;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaUserRepository;
import ru.tramplin_itplanet.tramplin.domain.exception.ApplicantNotFoundException;
import ru.tramplin_itplanet.tramplin.domain.exception.ApplicantOpportunityRecommendationAlreadyExistsException;
import ru.tramplin_itplanet.tramplin.domain.exception.InvalidApplicantOpportunityRecommendationOperationException;
import ru.tramplin_itplanet.tramplin.domain.exception.OpportunityNotFoundException;
import ru.tramplin_itplanet.tramplin.domain.model.ApplicantContactStatus;
import ru.tramplin_itplanet.tramplin.domain.model.ApplicantOpportunityRecommendation;
import ru.tramplin_itplanet.tramplin.domain.model.UserRole;
import ru.tramplin_itplanet.tramplin.domain.service.ApplicantOpportunityRecommendationService;

@Service
@Transactional(readOnly = true)
public class ApplicantOpportunityRecommendationServiceImpl implements ApplicantOpportunityRecommendationService {

    private static final Logger log = LoggerFactory.getLogger(ApplicantOpportunityRecommendationServiceImpl.class);

    private final JpaUserRepository jpaUserRepository;
    private final JpaApplicantRepository jpaApplicantRepository;
    private final JpaOpportunityRepository jpaOpportunityRepository;
    private final JpaApplicantContactRepository jpaApplicantContactRepository;
    private final JpaApplicantOpportunityRecommendationRepository jpaRecommendationRepository;

    public ApplicantOpportunityRecommendationServiceImpl(
            JpaUserRepository jpaUserRepository,
            JpaApplicantRepository jpaApplicantRepository,
            JpaOpportunityRepository jpaOpportunityRepository,
            JpaApplicantContactRepository jpaApplicantContactRepository,
            JpaApplicantOpportunityRecommendationRepository jpaRecommendationRepository
    ) {
        this.jpaUserRepository = jpaUserRepository;
        this.jpaApplicantRepository = jpaApplicantRepository;
        this.jpaOpportunityRepository = jpaOpportunityRepository;
        this.jpaApplicantContactRepository = jpaApplicantContactRepository;
        this.jpaRecommendationRepository = jpaRecommendationRepository;
    }

    @Override
    @Transactional
    public ApplicantOpportunityRecommendation create(String userEmail, Long opportunityId, Long recommendedApplicantId) {
        log.info(
                "Creating applicant recommendation: userEmail={}, opportunityId={}, recommendedApplicantId={}",
                userEmail,
                opportunityId,
                recommendedApplicantId
        );

        ApplicantEntity recommender = resolveCurrentApplicant(userEmail);
        ApplicantEntity recommendedApplicant = jpaApplicantRepository.findById(recommendedApplicantId)
                .orElseThrow(() -> new ApplicantNotFoundException(recommendedApplicantId));

        if (recommender.getId().equals(recommendedApplicant.getId())) {
            throw new InvalidApplicantOpportunityRecommendationOperationException(
                    "You cannot recommend yourself for an opportunity"
            );
        }

        boolean isVerifiedContact = jpaApplicantContactRepository.existsBetweenApplicantsWithStatus(
                recommender.getId(),
                recommendedApplicant.getId(),
                ApplicantContactStatus.ACCEPTED
        );
        if (!isVerifiedContact) {
            throw new InvalidApplicantOpportunityRecommendationOperationException(
                    "Recommended applicant must be in your verified contacts"
            );
        }

        OpportunityEntity opportunity = jpaOpportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new OpportunityNotFoundException(opportunityId));

        if (jpaRecommendationRepository.existsRecommendation(recommender.getId(), recommendedApplicant.getId(), opportunityId)) {
            throw new ApplicantOpportunityRecommendationAlreadyExistsException(
                    recommender.getId(),
                    recommendedApplicant.getId(),
                    opportunityId
            );
        }

        ApplicantOpportunityRecommendationEntity entity = new ApplicantOpportunityRecommendationEntity();
        entity.setRecommender(recommender);
        entity.setRecommendedApplicant(recommendedApplicant);
        entity.setOpportunity(opportunity);

        ApplicantOpportunityRecommendationEntity saved = jpaRecommendationRepository.save(entity);
        return toDomain(saved);
    }

    private ApplicantEntity resolveCurrentApplicant(String userEmail) {
        UserEntity user = jpaUserRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.warn("Authenticated user not found by email={}", userEmail);
                    return new BadCredentialsException("Invalid authentication token");
                });

        if (user.getRole() != UserRole.APPLICANT) {
            throw new AccessDeniedException("User role must be APPLICANT");
        }

        return jpaApplicantRepository.findByUserIdWithSkills(user.getId())
                .orElseThrow(() -> new ApplicantNotFoundException(user.getId()));
    }

    private static ApplicantOpportunityRecommendation toDomain(ApplicantOpportunityRecommendationEntity entity) {
        return new ApplicantOpportunityRecommendation(
                entity.getId(),
                entity.getRecommender().getId(),
                entity.getRecommendedApplicant().getId(),
                entity.getOpportunity().getId(),
                entity.getCreatedAt()
        );
    }
}
