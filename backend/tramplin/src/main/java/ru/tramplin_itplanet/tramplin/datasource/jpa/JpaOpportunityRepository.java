package ru.tramplin_itplanet.tramplin.datasource.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.tramplin_itplanet.tramplin.datasource.entity.OpportunityEntity;

import java.util.List;
import java.util.Optional;

public interface JpaOpportunityRepository extends JpaRepository<OpportunityEntity, Long> {

    @Query("SELECT DISTINCT o FROM OpportunityEntity o " +
           "JOIN FETCH o.employer " +
           "LEFT JOIN FETCH o.tags " +
           "ORDER BY o.publishedAt DESC, o.id DESC")
    List<OpportunityEntity> findAllWithDetailsOrderByPublishedAtDesc();

    @Query("SELECT DISTINCT o FROM OpportunityEntity o " +
           "JOIN FETCH o.employer " +
           "LEFT JOIN FETCH o.tags " +
           "WHERE o.id = :id")
    Optional<OpportunityEntity> findByIdWithDetails(@Param("id") Long id);
}
