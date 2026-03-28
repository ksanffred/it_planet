package ru.tramplin_itplanet.tramplin.datasource.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.tramplin_itplanet.tramplin.datasource.entity.EmployerEntity;

import java.util.Optional;

public interface JpaEmployerRepository extends JpaRepository<EmployerEntity, Long> {
    Optional<EmployerEntity> findByUserId(Long userId);
}
