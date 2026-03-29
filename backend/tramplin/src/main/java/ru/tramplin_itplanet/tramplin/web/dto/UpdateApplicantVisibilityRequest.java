package ru.tramplin_itplanet.tramplin.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import ru.tramplin_itplanet.tramplin.domain.model.ApplicantVisibility;

@Schema(description = "Update current applicant visibility")
public record UpdateApplicantVisibilityRequest(
        @NotNull
        @Schema(description = "Visibility status", example = "PUBLIC", allowableValues = {"PUBLIC", "PRIVATE"})
        ApplicantVisibility visibility
) {}
