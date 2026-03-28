package ru.tramplin_itplanet.tramplin.di;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tramplin_itplanet.tramplin.datasource.entity.ApplicantEntity;
import ru.tramplin_itplanet.tramplin.datasource.entity.OpportunityEntity;
import ru.tramplin_itplanet.tramplin.datasource.entity.OpportunityResponseEntity;
import ru.tramplin_itplanet.tramplin.datasource.entity.UserEntity;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaApplicantRepository;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaOpportunityRepository;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaOpportunityResponseRepository;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaUserRepository;
import ru.tramplin_itplanet.tramplin.domain.exception.ApplicantNotFoundException;
import ru.tramplin_itplanet.tramplin.domain.exception.OpportunityNotFoundException;
import ru.tramplin_itplanet.tramplin.domain.exception.OpportunityResponseAlreadyExistsException;
import ru.tramplin_itplanet.tramplin.domain.model.ApplicantOpportunityResponseCard;
import ru.tramplin_itplanet.tramplin.domain.model.OpportunityResponse;
import ru.tramplin_itplanet.tramplin.domain.model.OpportunityResponseStatus;
import ru.tramplin_itplanet.tramplin.domain.model.UserRole;
import ru.tramplin_itplanet.tramplin.domain.service.OpportunityResponseService;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class OpportunityResponseServiceImpl implements OpportunityResponseService {

    private static final Logger log = LoggerFactory.getLogger(OpportunityResponseServiceImpl.class);

    private final JpaUserRepository jpaUserRepository;
    private final JpaApplicantRepository jpaApplicantRepository;
    private final JpaOpportunityRepository jpaOpportunityRepository;
    private final JpaOpportunityResponseRepository jpaOpportunityResponseRepository;

    public OpportunityResponseServiceImpl(
            JpaUserRepository jpaUserRepository,
            JpaApplicantRepository jpaApplicantRepository,
            JpaOpportunityRepository jpaOpportunityRepository,
            JpaOpportunityResponseRepository jpaOpportunityResponseRepository
    ) {
        this.jpaUserRepository = jpaUserRepository;
        this.jpaApplicantRepository = jpaApplicantRepository;
        this.jpaOpportunityRepository = jpaOpportunityRepository;
        this.jpaOpportunityResponseRepository = jpaOpportunityResponseRepository;
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
}
