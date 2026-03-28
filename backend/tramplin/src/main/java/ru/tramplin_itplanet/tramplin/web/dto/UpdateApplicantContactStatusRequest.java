package ru.tramplin_itplanet.tramplin.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import ru.tramplin_itplanet.tramplin.domain.model.ApplicantContactStatus;

@Schema(description = "Update contact request status")
public record UpdateApplicantContactStatusRequest(
        @NotNull
        @Schema(description = "New status", allowableValues = {"ACCEPTED", "REJECTED"}, example = "ACCEPTED")
        ApplicantContactStatus status
) {}
