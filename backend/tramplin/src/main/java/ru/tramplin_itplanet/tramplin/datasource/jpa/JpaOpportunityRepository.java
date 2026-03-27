package ru.tramplin_itplanet.tramplin.datasource.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.tramplin_itplanet.tramplin.datasource.entity.OpportunityEntity;
import ru.tramplin_itplanet.tramplin.domain.model.OpportunityStatus;

import java.util.List;
import java.util.Optional;

public interface JpaOpportunityRepository extends JpaRepository<OpportunityEntity, Long> {

    @Query("SELECT o.id FROM OpportunityEntity o " +
           "WHERE o.status = :status " +
           "ORDER BY o.publishedAt DESC, o.id DESC")
    List<Long> findIdsByStatusOrderByPublishedAtDesc(@Param("status") OpportunityStatus status);

    @Query(value = """
            SELECT o.id
            FROM opportunities o
            JOIN employers e ON e.id = o.employer_id
            LEFT JOIN opportunity_tags ot ON ot.opportunity_id = o.id
            LEFT JOIN tags t ON t.id = ot.tag_id
            WHERE o.status = :status
            GROUP BY o.id, o.published_at, o.title, o.description, e.company_name
            HAVING (
                setweight(to_tsvector('russian', coalesce(o.title, '')), 'A') ||
                setweight(to_tsvector('english', coalesce(o.title, '')), 'A') ||
                setweight(to_tsvector('russian', coalesce(o.description, '')), 'B') ||
                setweight(to_tsvector('english', coalesce(o.description, '')), 'B') ||
                setweight(to_tsvector('russian', coalesce(e.company_name, '')), 'A') ||
                setweight(to_tsvector('english', coalesce(e.company_name, '')), 'A') ||
                setweight(to_tsvector('russian', coalesce(string_agg(DISTINCT t.name, ' '), '')), 'C') ||
                setweight(to_tsvector('english', coalesce(string_agg(DISTINCT t.name, ' '), '')), 'C')
            ) @@ (websearch_to_tsquery('russian', :search) || websearch_to_tsquery('english', :search))
            ORDER BY o.published_at DESC NULLS LAST, o.id DESC
            """, nativeQuery = true)
    List<Long> findIdsByStatusAndSearchOrderByPublishedAtDesc(
            @Param("status") String status,
            @Param("search") String search
    );

    @Query("SELECT DISTINCT o FROM OpportunityEntity o " +
           "JOIN FETCH o.employer " +
           "LEFT JOIN FETCH o.tags " +
           "WHERE o.status = :status AND o.id IN :ids")
    List<OpportunityEntity> findAllByIdInAndStatusWithDetails(
            @Param("ids") List<Long> ids,
            @Param("status") OpportunityStatus status
    );

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
