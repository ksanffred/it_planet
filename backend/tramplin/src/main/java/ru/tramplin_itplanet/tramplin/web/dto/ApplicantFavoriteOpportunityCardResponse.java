package ru.tramplin_itplanet.tramplin.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Opportunity card from applicant favorites")
public record ApplicantFavoriteOpportunityCardResponse(
        @Schema(description = "Opportunity title", example = "Java Developer")
        String title,

        @JsonProperty("company_name")
        @Schema(description = "Company name", example = "Acme Corp")
        String companyName,

        @Schema(description = "Opportunity status", example = "ACTIVE")
        String status,

        @Schema(description = "Opportunity type", example = "VACANCY")
        String type
) {}
