package ru.tramplin_itplanet.tramplin.datasource.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "applicant_favorite_opportunities")
public class ApplicantFavoriteOpportunityEntity {

    @EmbeddedId
    private ApplicantFavoriteOpportunityId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("applicantId")
    @JoinColumn(name = "applicant_id", nullable = false)
    private ApplicantEntity applicant;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("opportunityId")
    @JoinColumn(name = "opportunity_id", nullable = false)
    private OpportunityEntity opportunity;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public ApplicantFavoriteOpportunityId getId() {
        return id;
    }

    public void setId(ApplicantFavoriteOpportunityId id) {
        this.id = id;
    }

    public ApplicantEntity getApplicant() {
        return applicant;
    }

    public void setApplicant(ApplicantEntity applicant) {
        this.applicant = applicant;
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
