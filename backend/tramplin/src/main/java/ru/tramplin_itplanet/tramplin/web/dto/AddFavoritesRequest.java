package ru.tramplin_itplanet.tramplin.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Request payload for adding multiple opportunities to favorites")
public record AddFavoritesRequest(
        @NotEmpty
        @Schema(description = "Opportunity card IDs to add to favorites", example = "[1, 2, 3]")
        List<@NotNull Long> opportunityIds
) {}
