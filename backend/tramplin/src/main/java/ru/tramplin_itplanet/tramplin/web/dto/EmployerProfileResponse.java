package ru.tramplin_itplanet.tramplin.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Employer profile")
public record EmployerProfileResponse(
        @Schema(description = "Unique identifier", example = "1")
        Long id,

        @Schema(description = "Related user id", example = "12")
        Long userId,

        @Schema(description = "Employer company name", example = "Acme Corp")
        String companyName,

        @Schema(description = "Employer description", example = "Global software company")
        String description,

        @Schema(description = "Tax ID (INN)", example = "7701234567")
        String inn,

        @Schema(description = "Company website", example = "https://acme.com")
        String website,

        @Schema(description = "Social links or contacts", example = "@acme_hr")
        String socials,

        @Schema(description = "Logo URL", example = "https://acme.com/logo.png")
        String logoUrl,

        @Schema(description = "Verification status", example = "pending")
        String status
) {}
