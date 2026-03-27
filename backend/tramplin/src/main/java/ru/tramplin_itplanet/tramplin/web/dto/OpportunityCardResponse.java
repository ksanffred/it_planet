package ru.tramplin_itplanet.tramplin.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Full opportunity card displayed to the user")
public record OpportunityCardResponse(

        @Schema(description = "Unique identifier of the opportunity", example = "1")
        Long id,

        @Schema(description = "Title of the position or event", example = "Junior Java Developer")
        String title,

        @Schema(description = "Full description including requirements and details", example = "Looking for a motivated developer...")
        String description,

        @Schema(description = "List of media URLs (images, videos) attached to the opportunity")
        List<String> media,

        @Schema(description = "Employer or event organiser details")
        EmployerResponse employer,

        @Schema(description = "Opportunity type", example = "VACANCY", allowableValues = {"VACANCY", "INTERNSHIP", "MENTORSHIP", "EVENT"})
        String type,

        @Schema(description = "Work format", example = "REMOTE", allowableValues = {"OFFICE", "HYBRID", "REMOTE"})
        String format,

        @Schema(description = "Exact office address for office/hybrid opportunities", example = "Tverskaya St, 1")
        String address,

        @Schema(description = "City of the opportunity or event", example = "Moscow")
        String city,

        @Schema(description = "Minimum salary in RUB", example = "100000")
        BigDecimal salaryFrom,

        @Schema(description = "Maximum salary in RUB", example = "150000")
        BigDecimal salaryTo,

        @Schema(description = "Date when the opportunity was published")
        LocalDateTime publishedAt,

        @Schema(description = "Closing date for the vacancy or date of the event")
        LocalDateTime expiresAt,

        @Schema(description = "Current status", example = "ACTIVE", allowableValues = {"ACTIVE", "CLOSED", "PLANNED"})
        String status,

        @Schema(description = "Assigned tags: technologies, level, employment type")
        List<TagResponse> tags
) {}
