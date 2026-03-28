package ru.tramplin_itplanet.tramplin.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Created response to an opportunity")
public record OpportunityResponseCreatedResponse(
        @Schema(description = "Response id", example = "1")
        Long id,

        @Schema(description = "Opportunity id", example = "10")
        Long opportunityId,

        @Schema(description = "Applicant id", example = "5")
        Long applicantId,

        @Schema(description = "Current response status", example = "NOT_REVIEWED")
        String status,

        @Schema(description = "Creation time")
        LocalDateTime createdAt,

        @Schema(description = "Last update time")
        LocalDateTime updatedAt
) {}
