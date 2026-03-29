package ru.tramplin_itplanet.tramplin.datasource.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.tramplin_itplanet.tramplin.datasource.entity.CuratorEntity;

public interface JpaCuratorRepository extends JpaRepository<CuratorEntity, Long> {
    boolean existsByUserId(Long userId);
}
