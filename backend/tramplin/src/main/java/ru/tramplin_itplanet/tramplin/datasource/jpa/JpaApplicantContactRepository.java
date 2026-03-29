package ru.tramplin_itplanet.tramplin.datasource.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.tramplin_itplanet.tramplin.datasource.entity.ApplicantContactEntity;
import ru.tramplin_itplanet.tramplin.domain.model.ApplicantContactStatus;

import java.util.List;

public interface JpaApplicantContactRepository extends JpaRepository<ApplicantContactEntity, Long> {

    @Query("SELECT COUNT(c) > 0 FROM ApplicantContactEntity c " +
           "WHERE (c.requester.id = :firstId AND c.recipient.id = :secondId) " +
           "OR (c.requester.id = :secondId AND c.recipient.id = :firstId)")
    boolean existsBetweenApplicants(@Param("firstId") Long firstId, @Param("secondId") Long secondId);

    @Query("SELECT COUNT(c) > 0 FROM ApplicantContactEntity c " +
           "WHERE ((c.requester.id = :firstId AND c.recipient.id = :secondId) " +
           "OR (c.requester.id = :secondId AND c.recipient.id = :firstId)) " +
           "AND c.status = :status")
    boolean existsBetweenApplicantsWithStatus(
            @Param("firstId") Long firstId,
            @Param("secondId") Long secondId,
            @Param("status") ApplicantContactStatus status
    );

    @Query("SELECT c FROM ApplicantContactEntity c " +
           "JOIN FETCH c.requester " +
           "JOIN FETCH c.recipient " +
           "WHERE (c.requester.id = :applicantId OR c.recipient.id = :applicantId) " +
           "AND c.status IN :statuses " +
           "ORDER BY c.updatedAt DESC, c.id DESC")
    List<ApplicantContactEntity> findByApplicantIdAndStatusesWithApplicants(
            @Param("applicantId") Long applicantId,
            @Param("statuses") List<ApplicantContactStatus> statuses
    );
}
