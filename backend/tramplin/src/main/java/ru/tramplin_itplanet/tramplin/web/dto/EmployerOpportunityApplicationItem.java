package ru.tramplin_itplanet.tramplin.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Application item for employer opportunity")
public record EmployerOpportunityApplicationItem(
        @Schema(description = "Response id", example = "11")
        Long responseId,

        @Schema(description = "Opportunity id", example = "10")
        Long opportunityId,

        @Schema(description = "Opportunity title", example = "Java Developer")
        String title,

        @JsonProperty("company_name")
        @Schema(description = "Company name", example = "Acme Corp")
        String companyName,

        @JsonProperty("response_status")
        @Schema(description = "Applicant response status", example = "NOT_REVIEWED")
        String responseStatus,

        @JsonProperty("opportunity_type")
        @Schema(description = "Opportunity type", example = "VACANCY")
        String opportunityType,

        @JsonProperty("opportunity_status")
        @Schema(description = "Opportunity status", example = "ACTIVE")
        String opportunityStatus,

        @JsonProperty("applicant_id")
        @Schema(description = "Applicant id", example = "3")
        Long applicantId,

        @JsonProperty("applicant_name")
        @Schema(description = "Applicant name", example = "Ivan Ivanov")
        String applicantName,

        @JsonProperty("applied_at")
        @Schema(description = "Application creation time")
        LocalDateTime appliedAt
) {}
