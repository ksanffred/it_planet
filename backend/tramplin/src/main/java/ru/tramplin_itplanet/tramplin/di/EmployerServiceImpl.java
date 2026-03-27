package ru.tramplin_itplanet.tramplin.di;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.tramplin_itplanet.tramplin.datasource.entity.EmployerEntity;
import ru.tramplin_itplanet.tramplin.datasource.entity.UserEntity;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaEmployerRepository;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaUserRepository;
import ru.tramplin_itplanet.tramplin.domain.exception.EmployerNotFoundException;
import ru.tramplin_itplanet.tramplin.domain.exception.UserNotFoundException;
import ru.tramplin_itplanet.tramplin.domain.model.CreateEmployerCommand;
import ru.tramplin_itplanet.tramplin.domain.model.EmployerProfile;
import ru.tramplin_itplanet.tramplin.domain.service.EmployerService;

@Service
public class EmployerServiceImpl implements EmployerService {

    private static final Logger log = LoggerFactory.getLogger(EmployerServiceImpl.class);
    private static final String STATUS_PENDING = "pending";
    private static final String STATUS_AUTO_VERIFIED = "auto_verified";
    private static final String STATUS_AUTO_REJECTED = "auto_rejected";

    private final JpaEmployerRepository jpaEmployerRepository;
    private final JpaUserRepository jpaUserRepository;
    private final WhoisClient whoisClient;

    public EmployerServiceImpl(JpaEmployerRepository jpaEmployerRepository,
                               JpaUserRepository jpaUserRepository,
                               WhoisClient whoisClient) {
        this.jpaEmployerRepository = jpaEmployerRepository;
        this.jpaUserRepository = jpaUserRepository;
        this.whoisClient = whoisClient;
    }

    @Override
    public EmployerProfile register(CreateEmployerCommand command) {
        log.info("Registering employer with inn={}", command.inn());

        UserEntity user = resolveUser(command.userId());
        VerificationDecision verificationDecision = evaluateAutoVerification(user, command.inn());

        EmployerEntity entity = new EmployerEntity();
        entity.setUserId(command.userId());
        entity.setName(command.companyName());
        entity.setDescription(command.description());
        entity.setInn(command.inn());
        entity.setWebsite(command.website());
        entity.setSocials(command.socials());
        entity.setLogoUrl(command.logoUrl());
        entity.setVerifiedOrgName(verificationDecision.verifiedOrgName());
        entity.setStatus(verificationDecision.status());

        EmployerEntity saved = jpaEmployerRepository.save(entity);
        return toProfile(saved);
    }

    @Override
    public EmployerProfile getById(Long id) {
        log.info("Loading employer by id={}", id);
        EmployerEntity entity = jpaEmployerRepository.findById(id)
                .orElseThrow(() -> new EmployerNotFoundException(id));
        return toProfile(entity);
    }

    private static EmployerProfile toProfile(EmployerEntity entity) {
        return new EmployerProfile(
                entity.getId(),
                entity.getUserId(),
                entity.getName(),
                entity.getDescription(),
                entity.getInn(),
                entity.getWebsite(),
                entity.getSocials(),
                entity.getLogoUrl(),
                entity.getVerifiedOrgName(),
                entity.getStatus()
        );
    }

    private UserEntity resolveUser(Long userId) {
        if (userId == null) {
            return null;
        }
        return jpaUserRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("Employer registration failed: user not found, userId={}", userId);
                    return new UserNotFoundException(userId);
                });
    }

    private VerificationDecision evaluateAutoVerification(UserEntity user, String inn) {
        if (user == null) {
            return new VerificationDecision(STATUS_PENDING, null);
        }
        if (!user.isVerified()) {
            log.info("User email is not verified yet, keeping employer status pending: userId={}", user.getId());
            return new VerificationDecision(STATUS_PENDING, null);
        }
        String domain = extractDomain(user.getEmail());
        if (domain == null) {
            log.warn("Cannot extract domain from user email, auto-rejecting employer: userId={}", user.getId());
            return new VerificationDecision(STATUS_AUTO_REJECTED, null);
        }
        return whoisClient.findVerificationDataByDomain(domain)
                .map(whoisData -> {
                    if (whoisData.taxpayerId().equals(inn)) {
                        return new VerificationDecision(STATUS_AUTO_VERIFIED, whoisData.organizationName());
                    }
                    return new VerificationDecision(STATUS_AUTO_REJECTED, whoisData.organizationName());
                })
                .orElseGet(() -> new VerificationDecision(STATUS_AUTO_REJECTED, null));
    }

    private static String extractDomain(String email) {
        if (email == null) {
            return null;
        }
        int atIndex = email.indexOf('@');
        if (atIndex < 0 || atIndex == email.length() - 1) {
            return null;
        }
        return email.substring(atIndex + 1).trim().toLowerCase();
    }

    private record VerificationDecision(String status, String verifiedOrgName) {}
}
