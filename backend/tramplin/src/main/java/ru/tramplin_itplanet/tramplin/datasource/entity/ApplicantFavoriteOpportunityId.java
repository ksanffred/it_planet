package ru.tramplin_itplanet.tramplin.datasource.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ApplicantFavoriteOpportunityId implements Serializable {

    @Column(name = "applicant_id", nullable = false)
    private Long applicantId;

    @Column(name = "opportunity_id", nullable = false)
    private Long opportunityId;

    public ApplicantFavoriteOpportunityId() {
    }

    public ApplicantFavoriteOpportunityId(Long applicantId, Long opportunityId) {
        this.applicantId = applicantId;
        this.opportunityId = opportunityId;
    }

    public Long getApplicantId() {
        return applicantId;
    }

    public void setApplicantId(Long applicantId) {
        this.applicantId = applicantId;
    }

    public Long getOpportunityId() {
        return opportunityId;
    }

    public void setOpportunityId(Long opportunityId) {
        this.opportunityId = opportunityId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ApplicantFavoriteOpportunityId that)) {
            return false;
        }
        return Objects.equals(applicantId, that.applicantId)
                && Objects.equals(opportunityId, that.opportunityId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(applicantId, opportunityId);
    }
}
