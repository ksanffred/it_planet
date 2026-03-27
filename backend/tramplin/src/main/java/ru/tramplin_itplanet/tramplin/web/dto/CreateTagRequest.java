package ru.tramplin_itplanet.tramplin.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import ru.tramplin_itplanet.tramplin.domain.model.TagCategory;

@Schema(description = "Request body for creating a new tag")
public record CreateTagRequest(

        @NotBlank
        @Schema(description = "Tag name", example = "Docker")
        String name,

        @NotNull
        @Schema(description = "Tag category", example = "TECHNOLOGY",
                allowableValues = {"TECHNOLOGY", "LEVEL", "EMPLOYMENT_TYPE"})
        TagCategory category
) {}
