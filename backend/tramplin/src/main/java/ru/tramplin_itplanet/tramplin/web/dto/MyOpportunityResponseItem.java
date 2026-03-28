package ru.tramplin_itplanet.tramplin.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Opportunity item to which applicant has responded")
public record MyOpportunityResponseItem(
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
        String opportunityStatus
) {}
