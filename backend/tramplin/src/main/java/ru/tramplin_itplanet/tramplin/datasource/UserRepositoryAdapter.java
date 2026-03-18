package ru.tramplin_itplanet.tramplin.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import ru.tramplin_itplanet.tramplin.datasource.entity.UserEntity;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaUserRepository;
import ru.tramplin_itplanet.tramplin.datasource.mapper.UserEntityMapper;
import ru.tramplin_itplanet.tramplin.domain.model.User;
import ru.tramplin_itplanet.tramplin.domain.model.UserRole;
import ru.tramplin_itplanet.tramplin.domain.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public class UserRepositoryAdapter implements UserRepository {

    private static final Logger log = LoggerFactory.getLogger(UserRepositoryAdapter.class);

    private final JpaUserRepository jpaUserRepository;

    public UserRepositoryAdapter(JpaUserRepository jpaUserRepository) {
        this.jpaUserRepository = jpaUserRepository;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        log.debug("Looking up user by email: {}", email);
        return jpaUserRepository.findByEmail(email).map(UserEntityMapper::toDomain);
    }

    @Override
    public User save(String email, String displayName, String passwordHash, UserRole role) {
        log.debug("Saving new user with email: {}", email);
        UserEntity entity = new UserEntity();
        entity.setEmail(email);
        entity.setDisplayName(displayName);
        entity.setPasswordHash(passwordHash);
        entity.setRole(role);
        entity.setVerified(false);
        entity.setCreatedAt(LocalDateTime.now());
        User saved = UserEntityMapper.toDomain(jpaUserRepository.save(entity));
        log.info("User saved with id={}, email={}, role={}", saved.id(), saved.email(), saved.role());
        return saved;
    }
}
