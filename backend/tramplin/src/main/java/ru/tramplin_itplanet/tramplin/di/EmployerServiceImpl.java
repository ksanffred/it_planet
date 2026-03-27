package ru.tramplin_itplanet.tramplin.di;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.tramplin_itplanet.tramplin.datasource.entity.EmployerEntity;
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

    private final JpaEmployerRepository jpaEmployerRepository;
    private final JpaUserRepository jpaUserRepository;

    public EmployerServiceImpl(JpaEmployerRepository jpaEmployerRepository,
                               JpaUserRepository jpaUserRepository) {
        this.jpaEmployerRepository = jpaEmployerRepository;
        this.jpaUserRepository = jpaUserRepository;
    }

    @Override
    public EmployerProfile register(CreateEmployerCommand command) {
        log.info("Registering employer with inn={}", command.inn());
        if (command.userId() != null && !jpaUserRepository.existsById(command.userId())) {
            log.warn("Employer registration failed: user not found, userId={}", command.userId());
            throw new UserNotFoundException(command.userId());
        }

        EmployerEntity entity = new EmployerEntity();
        entity.setUserId(command.userId());
        entity.setName(command.companyName());
        entity.setDescription(command.description());
        entity.setInn(command.inn());
        entity.setWebsite(command.website());
        entity.setSocials(command.socials());
        entity.setLogoUrl(command.logoUrl());
        entity.setStatus("pending");

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
                entity.getStatus()
        );
    }
}
