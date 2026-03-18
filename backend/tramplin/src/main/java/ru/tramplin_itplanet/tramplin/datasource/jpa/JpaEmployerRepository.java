package ru.tramplin_itplanet.tramplin.datasource.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.tramplin_itplanet.tramplin.datasource.entity.EmployerEntity;

public interface JpaEmployerRepository extends JpaRepository<EmployerEntity, Long> {}
