package ru.tramplin_itplanet.tramplin.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Mini-card for home page feed and map overlays")
public record OpportunityMiniCardResponse(

        @Schema(description = "Unique identifier of the opportunity", example = "1")
        Long id,

        @Schema(description = "First media item URL from the opportunity media list", example = "https://cdn.tramplin.ru/media/opportunity-1.png")
        String media,

        @Schema(description = "Opportunity title", example = "Junior Java Developer")
        String title,

        @Schema(description = "Short opportunity description", example = "Backend role in a fintech team")
        String description,

        @Schema(description = "Employer name", example = "Acme Corp")
        String employerName,

        @Schema(description = "Work format", example = "REMOTE", allowableValues = {"OFFICE", "HYBRID", "REMOTE"})
        String format,

        @Schema(description = "First three tags from the opportunity")
        List<String> tags
) {}
