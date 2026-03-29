package ru.tramplin_itplanet.tramplin.di;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tramplin_itplanet.tramplin.datasource.entity.CuratorEntity;
import ru.tramplin_itplanet.tramplin.datasource.entity.UserEntity;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaCuratorRepository;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaUserRepository;
import ru.tramplin_itplanet.tramplin.domain.exception.CuratorAlreadyExistsException;
import ru.tramplin_itplanet.tramplin.domain.exception.CuratorNotFoundException;
import ru.tramplin_itplanet.tramplin.domain.exception.UserNotFoundException;
import ru.tramplin_itplanet.tramplin.domain.model.Curator;
import ru.tramplin_itplanet.tramplin.domain.model.UserRole;
import ru.tramplin_itplanet.tramplin.domain.service.CuratorService;

@Service
@Transactional(readOnly = true)
public class CuratorServiceImpl implements CuratorService {

    private static final Logger log = LoggerFactory.getLogger(CuratorServiceImpl.class);
    private static final Long ROOT_CURATOR_ID = 1L;

    private final JpaUserRepository jpaUserRepository;
    private final JpaCuratorRepository jpaCuratorRepository;

    public CuratorServiceImpl(JpaUserRepository jpaUserRepository, JpaCuratorRepository jpaCuratorRepository) {
        this.jpaUserRepository = jpaUserRepository;
        this.jpaCuratorRepository = jpaCuratorRepository;
    }

    @Override
    @Transactional
    public Curator create(String requesterEmail, Long userId) {
        log.info("Creating curator: requesterEmail={}, userId={}", requesterEmail, userId);
        assertRequesterIsRootCurator(requesterEmail);

        UserEntity targetUser = jpaUserRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (jpaCuratorRepository.existsByUserId(userId)) {
            throw new CuratorAlreadyExistsException(userId);
        }

        targetUser.setRole(UserRole.CURATOR);
        jpaUserRepository.save(targetUser);

        CuratorEntity entity = new CuratorEntity();
        entity.setUserId(userId);

        CuratorEntity saved = jpaCuratorRepository.save(entity);
        return new Curator(saved.getId(), saved.getUserId());
    }

    @Override
    @Transactional
    public void delete(String requesterEmail, Long curatorId) {
        log.info("Deleting curator: requesterEmail={}, curatorId={}", requesterEmail, curatorId);
        assertRequesterIsRootCurator(requesterEmail);

        if (ROOT_CURATOR_ID.equals(curatorId)) {
            throw new AccessDeniedException("Curator with id 1 cannot be deleted");
        }

        CuratorEntity entity = jpaCuratorRepository.findById(curatorId)
                .orElseThrow(() -> new CuratorNotFoundException(curatorId));

        UserEntity user = jpaUserRepository.findById(entity.getUserId())
                .orElseThrow(() -> new UserNotFoundException(entity.getUserId()));
        user.setRole(UserRole.USER);
        jpaUserRepository.save(user);

        jpaCuratorRepository.delete(entity);
    }

    private void assertRequesterIsRootCurator(String requesterEmail) {
        UserEntity requester = jpaUserRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> {
                    log.warn("Authenticated user not found by email={}", requesterEmail);
                    return new BadCredentialsException("Invalid authentication token");
                });

        if (requester.getRole() != UserRole.CURATOR) {
            throw new AccessDeniedException("Only CURATOR role can manage curators");
        }

        CuratorEntity rootCurator = jpaCuratorRepository.findById(ROOT_CURATOR_ID)
                .orElseThrow(() -> new AccessDeniedException("Root curator is not configured"));

        if (!rootCurator.getUserId().equals(requester.getId())) {
            throw new AccessDeniedException("Only curator with id 1 can manage curators");
        }
    }
}
