package ru.tramplin_itplanet.tramplin.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Employer or event organiser")
public record EmployerResponse(

        @Schema(description = "Unique identifier", example = "1")
        Long id,

        @Schema(description = "Company or organiser name", example = "Acme Corp")
        String name,

        @Schema(description = "URL of the company logo", example = "https://acme.com/logo.png")
        String logoUrl,

        @Schema(description = "Company website URL", example = "https://acme.com")
        String website,

        @Schema(description = "Contact information (email, phone, or Telegram)", example = "hr@acme.com")
        String contacts
) {}
