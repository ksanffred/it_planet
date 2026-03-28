package ru.tramplin_itplanet.tramplin.datasource.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.tramplin_itplanet.tramplin.datasource.entity.ApplicantEntity;

import java.util.Optional;

public interface JpaApplicantRepository extends JpaRepository<ApplicantEntity, Long> {

    @Query("SELECT DISTINCT a FROM ApplicantEntity a LEFT JOIN FETCH a.skills WHERE a.id = :id")
    Optional<ApplicantEntity> findByIdWithSkills(@Param("id") Long id);

    @Query("SELECT DISTINCT a FROM ApplicantEntity a LEFT JOIN FETCH a.skills WHERE a.userId = :userId")
    Optional<ApplicantEntity> findByUserIdWithSkills(@Param("userId") Long userId);
}
