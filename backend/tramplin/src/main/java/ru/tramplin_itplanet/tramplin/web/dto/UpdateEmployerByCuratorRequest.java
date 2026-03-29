package ru.tramplin_itplanet.tramplin.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request payload for curator full update of employer profile")
public record UpdateEmployerByCuratorRequest(
        @Schema(description = "Linked user id", example = "12")
        Long userId,

        @Schema(description = "Company name", example = "Acme Corp")
        String companyName,

        @Schema(description = "Employer description", example = "Global software company")
        String description,

        @Schema(description = "Taxpayer identification number", example = "7701234567")
        String inn,

        @Schema(description = "Company website", example = "https://acme.com")
        String website,

        @Schema(description = "Social links or contacts", example = "@acme_hr")
        String socials,

        @Schema(description = "Logo URL", example = "https://acme.com/logo.png")
        String logoUrl,

        @Schema(description = "Verified organization name", example = "Acme Corporation")
        String verifiedOrgName,

        @Schema(description = "Employer status", example = "full_verified")
        String status
) {}
