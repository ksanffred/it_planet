package ru.tramplin_itplanet.tramplin.datasource.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.tramplin_itplanet.tramplin.datasource.entity.ApplicantContactEntity;

public interface JpaApplicantContactRepository extends JpaRepository<ApplicantContactEntity, Long> {

    @Query("SELECT COUNT(c) > 0 FROM ApplicantContactEntity c " +
           "WHERE (c.requester.id = :firstId AND c.recipient.id = :secondId) " +
           "OR (c.requester.id = :secondId AND c.recipient.id = :firstId)")
    boolean existsBetweenApplicants(@Param("firstId") Long firstId, @Param("secondId") Long secondId);
}
