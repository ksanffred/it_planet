package ru.tramplin_itplanet.tramplin.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request payload for employer registration")
public record RegisterEmployerRequest(
        @Schema(description = "Related user id", example = "12")
        Long userId,

        @NotBlank
        @Schema(description = "Employer company name", example = "Acme Corp")
        String companyName,

        @Schema(description = "Employer description", example = "Global software company")
        String description,

        @NotBlank
        @Schema(description = "Tax ID (INN)", example = "7701234567", requiredMode = Schema.RequiredMode.REQUIRED)
        String inn,

        @Schema(description = "Company website", example = "https://acme.com")
        String website,

        @Schema(description = "Social links or contacts", example = "@acme_hr")
        String socials,

        @Schema(description = "Logo URL", example = "https://acme.com/logo.png")
        String logoUrl
) {}
