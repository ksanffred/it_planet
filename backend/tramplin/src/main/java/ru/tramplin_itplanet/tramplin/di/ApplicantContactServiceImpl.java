package ru.tramplin_itplanet.tramplin.di;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tramplin_itplanet.tramplin.datasource.entity.ApplicantContactEntity;
import ru.tramplin_itplanet.tramplin.datasource.entity.ApplicantEntity;
import ru.tramplin_itplanet.tramplin.datasource.entity.UserEntity;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaApplicantContactRepository;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaApplicantRepository;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaUserRepository;
import ru.tramplin_itplanet.tramplin.domain.exception.ApplicantContactAlreadyExistsException;
import ru.tramplin_itplanet.tramplin.domain.exception.ApplicantContactNotFoundException;
import ru.tramplin_itplanet.tramplin.domain.exception.ApplicantNotFoundException;
import ru.tramplin_itplanet.tramplin.domain.exception.InvalidApplicantContactOperationException;
import ru.tramplin_itplanet.tramplin.domain.model.ApplicantContact;
import ru.tramplin_itplanet.tramplin.domain.model.ApplicantContactStatus;
import ru.tramplin_itplanet.tramplin.domain.model.UserRole;
import ru.tramplin_itplanet.tramplin.domain.service.ApplicantContactService;

@Service
@Transactional(readOnly = true)
public class ApplicantContactServiceImpl implements ApplicantContactService {

    private static final Logger log = LoggerFactory.getLogger(ApplicantContactServiceImpl.class);

    private final JpaUserRepository jpaUserRepository;
    private final JpaApplicantRepository jpaApplicantRepository;
    private final JpaApplicantContactRepository jpaApplicantContactRepository;

    public ApplicantContactServiceImpl(JpaUserRepository jpaUserRepository,
                                       JpaApplicantRepository jpaApplicantRepository,
                                       JpaApplicantContactRepository jpaApplicantContactRepository) {
        this.jpaUserRepository = jpaUserRepository;
        this.jpaApplicantRepository = jpaApplicantRepository;
        this.jpaApplicantContactRepository = jpaApplicantContactRepository;
    }

    @Override
    @Transactional
    public ApplicantContact create(String userEmail, Long recipientApplicantId) {
        log.info("Creating applicant contact request: userEmail={}, recipientApplicantId={}", userEmail, recipientApplicantId);
        ApplicantEntity requester = resolveCurrentApplicant(userEmail);
        ApplicantEntity recipient = jpaApplicantRepository.findById(recipientApplicantId)
                .orElseThrow(() -> new ApplicantNotFoundException(recipientApplicantId));

        if (requester.getId().equals(recipient.getId())) {
            throw new InvalidApplicantContactOperationException("You cannot send contact request to yourself");
        }

        if (jpaApplicantContactRepository.existsBetweenApplicants(requester.getId(), recipient.getId())) {
            throw new ApplicantContactAlreadyExistsException(requester.getId(), recipient.getId());
        }

        ApplicantContactEntity entity = new ApplicantContactEntity();
        entity.setRequester(requester);
        entity.setRecipient(recipient);
        entity.setStatus(ApplicantContactStatus.PENDING);

        ApplicantContactEntity saved = jpaApplicantContactRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    @Transactional
    public ApplicantContact updateStatus(String userEmail, Long contactId, ApplicantContactStatus status) {
        log.info("Updating applicant contact status: userEmail={}, contactId={}, status={}", userEmail, contactId, status);
        ApplicantEntity currentApplicant = resolveCurrentApplicant(userEmail);

        if (status != ApplicantContactStatus.ACCEPTED && status != ApplicantContactStatus.REJECTED) {
            throw new InvalidApplicantContactOperationException("Contact status can only be ACCEPTED or REJECTED");
        }

        ApplicantContactEntity contact = jpaApplicantContactRepository.findById(contactId)
                .orElseThrow(() -> new ApplicantContactNotFoundException(contactId));

        if (!contact.getRecipient().getId().equals(currentApplicant.getId())) {
            throw new AccessDeniedException("Only invited applicant can accept or reject this contact request");
        }

        if (contact.getStatus() != ApplicantContactStatus.PENDING) {
            throw new InvalidApplicantContactOperationException("Only PENDING contact request can be updated");
        }

        contact.setStatus(status);
        ApplicantContactEntity updated = jpaApplicantContactRepository.save(contact);
        return toDomain(updated);
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

    private static ApplicantContact toDomain(ApplicantContactEntity entity) {
        return new ApplicantContact(
                entity.getId(),
                entity.getRequester().getId(),
                entity.getRecipient().getId(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
