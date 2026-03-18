package ru.tramplin_itplanet.tramplin.datasource.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.tramplin_itplanet.tramplin.datasource.entity.UserEntity;

import java.util.Optional;

public interface JpaUserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
}
