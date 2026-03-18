package ru.tramplin_itplanet.tramplin.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Tag assigned to an opportunity")
public record TagResponse(

        @Schema(description = "Unique identifier", example = "3")
        Long id,

        @Schema(description = "Tag name", example = "Java")
        String name,

        @Schema(description = "Tag category", example = "TECHNOLOGY",
                allowableValues = {"TECHNOLOGY", "LEVEL", "EMPLOYMENT_TYPE"})
        String category
) {}
