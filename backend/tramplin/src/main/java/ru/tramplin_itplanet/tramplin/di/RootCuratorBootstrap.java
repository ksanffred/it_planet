package ru.tramplin_itplanet.tramplin.di;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.tramplin_itplanet.tramplin.datasource.entity.UserEntity;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaUserRepository;
import ru.tramplin_itplanet.tramplin.domain.model.UserRole;

import java.time.LocalDateTime;

@Component
public class RootCuratorBootstrap {

    private static final Logger log = LoggerFactory.getLogger(RootCuratorBootstrap.class);
    private static final Long ROOT_CURATOR_ID = 1L;

    private final JpaUserRepository jpaUserRepository;
    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    @Value("${ROOT_CURATOR_EMAIL:}")
    private String rootCuratorEmail;

    @Value("${ROOT_CURATOR_PASSWORD:}")
    private String rootCuratorPassword;

    public RootCuratorBootstrap(
            JpaUserRepository jpaUserRepository,
            JdbcTemplate jdbcTemplate,
            PasswordEncoder passwordEncoder
    ) {
        this.jpaUserRepository = jpaUserRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    @Transactional
    public void bootstrap() {
        if (rootCuratorEmail == null || rootCuratorEmail.isBlank() || rootCuratorPassword == null || rootCuratorPassword.isBlank()) {
            log.warn("Root curator bootstrap skipped: ROOT_CURATOR_EMAIL or ROOT_CURATOR_PASSWORD is missing");
            return;
        }

        UserEntity rootUser = jpaUserRepository.findByEmail(rootCuratorEmail)
                .map(existing -> {
                    existing.setRole(UserRole.CURATOR);
                    existing.setVerified(true);
                    existing.setPasswordHash(passwordEncoder.encode(rootCuratorPassword));
                    return jpaUserRepository.save(existing);
                })
                .orElseGet(() -> {
                    UserEntity user = new UserEntity();
                    user.setEmail(rootCuratorEmail);
                    user.setDisplayName("Root Curator");
                    user.setPasswordHash(passwordEncoder.encode(rootCuratorPassword));
                    user.setRole(UserRole.CURATOR);
                    user.setVerified(true);
                    user.setCreatedAt(LocalDateTime.now());
                    return jpaUserRepository.save(user);
                });

        jdbcTemplate.update(
                """
                INSERT INTO curators (id, user_id)
                VALUES (?, ?)
                ON CONFLICT (id) DO UPDATE SET user_id = EXCLUDED.user_id
                """,
                ROOT_CURATOR_ID,
                rootUser.getId()
        );
        jdbcTemplate.execute(
                """
                SELECT setval('curators_id_seq', GREATEST((SELECT COALESCE(MAX(id), 1) FROM curators), 1))
                """
        );

        log.info("Root curator configured: curatorId={}, userId={}, email={}", ROOT_CURATOR_ID, rootUser.getId(), rootCuratorEmail);
    }
}
