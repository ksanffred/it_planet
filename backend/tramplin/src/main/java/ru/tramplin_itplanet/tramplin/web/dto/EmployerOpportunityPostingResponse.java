package ru.tramplin_itplanet.tramplin.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Employer opportunity in /opportunities/me list")
public record EmployerOpportunityPostingResponse(
        @Schema(description = "Opportunity id", example = "1")
        Long id,

        @Schema(description = "Opportunity title", example = "Java Developer")
        String title,

        @Schema(description = "Opportunity status", example = "ACTIVE")
        String status,

        @Schema(description = "Opportunity type", example = "VACANCY")
        String type,

        @JsonProperty("published_at")
        @Schema(description = "Published at")
        LocalDateTime publishedAt,

        @JsonProperty("expires_at")
        @Schema(description = "Expires at")
        LocalDateTime expiresAt,

        @JsonProperty("applications_count")
        @Schema(description = "Number of applications", example = "12")
        long applicationsCount
) {}
