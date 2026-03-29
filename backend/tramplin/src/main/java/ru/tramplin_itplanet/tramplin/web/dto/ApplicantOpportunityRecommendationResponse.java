package ru.tramplin_itplanet.tramplin.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Created recommendation between applicants for an opportunity")
public record ApplicantOpportunityRecommendationResponse(
        @Schema(description = "Recommendation id", example = "1")
        Long id,

        @Schema(description = "Recommender applicant id", example = "3")
        Long recommenderApplicantId,

        @Schema(description = "Recommended applicant id", example = "7")
        Long recommendedApplicantId,

        @Schema(description = "Opportunity card id", example = "10")
        Long opportunityId,

        @Schema(description = "Creation time")
        LocalDateTime createdAt
) {}
