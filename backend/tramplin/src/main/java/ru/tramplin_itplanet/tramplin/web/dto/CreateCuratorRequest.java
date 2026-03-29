package ru.tramplin_itplanet.tramplin.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Create curator request")
public record CreateCuratorRequest(
        @NotNull
        @Schema(description = "Existing user id to grant CURATOR role", example = "42")
        Long userId
) {}
