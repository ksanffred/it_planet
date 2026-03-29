package ru.tramplin_itplanet.tramplin.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request payload for updating a tag")
public record UpdateTagRequest(
        @NotBlank
        @Schema(description = "Tag name", example = "Docker")
        String name,

        @NotNull
        @Schema(description = "Tag category", example = "TECHNOLOGY")
        String category
) {}
