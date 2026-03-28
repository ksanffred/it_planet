package ru.tramplin_itplanet.tramplin.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request payload for updating current employer profile")
public record UpdateEmployerRequest(
        @Schema(description = "Employer description", example = "Global software company")
        String description,

        @Schema(description = "Company website", example = "https://acme.com")
        String website,

        @Schema(description = "Social links or contacts", example = "@acme_hr")
        String socials,

        @Schema(description = "Logo URL", example = "https://acme.com/logo.png")
        String logoUrl
) {}
