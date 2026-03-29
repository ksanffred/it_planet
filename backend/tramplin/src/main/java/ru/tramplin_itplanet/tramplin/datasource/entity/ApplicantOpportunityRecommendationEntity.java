package ru.tramplin_itplanet.tramplin.datasource.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "applicant_opportunity_recommendations")
public class ApplicantOpportunityRecommendationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommender_id", nullable = false)
    private ApplicantEntity recommender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommended_applicant_id", nullable = false)
    private ApplicantEntity recommendedApplicant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opportunity_id", nullable = false)
    private OpportunityEntity opportunity;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ApplicantEntity getRecommender() {
        return recommender;
    }

    public void setRecommender(ApplicantEntity recommender) {
        this.recommender = recommender;
    }

    public ApplicantEntity getRecommendedApplicant() {
        return recommendedApplicant;
    }

    public void setRecommendedApplicant(ApplicantEntity recommendedApplicant) {
        this.recommendedApplicant = recommendedApplicant;
    }

    public OpportunityEntity getOpportunity() {
        return opportunity;
    }

    public void setOpportunity(OpportunityEntity opportunity) {
        this.opportunity = opportunity;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
