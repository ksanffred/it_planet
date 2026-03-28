package ru.tramplin_itplanet.tramplin.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Applicant favorites snapshot")
public record ApplicantFavoritesResponse(
        @Schema(description = "Applicant profile id", example = "1")
        Long applicantId,

        @Schema(description = "All favorited opportunity IDs", example = "[12, 9, 5]")
        List<Long> opportunityIds
) {}
