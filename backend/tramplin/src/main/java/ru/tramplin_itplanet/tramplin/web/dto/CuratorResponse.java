package ru.tramplin_itplanet.tramplin.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Curator record")
public record CuratorResponse(
        @Schema(description = "Curator id", example = "2")
        Long id,

        @Schema(description = "Associated user id", example = "42")
        Long userId
) {}
