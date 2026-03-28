package ru.tramplin_itplanet.tramplin.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Applicant contact request")
public record ApplicantContactResponse(
        @Schema(description = "Contact request id", example = "1")
        Long id,

        @Schema(description = "Applicant id who sent request", example = "3")
        Long requesterApplicantId,

        @Schema(description = "Applicant id who received request", example = "7")
        Long recipientApplicantId,

        @Schema(description = "Contact status", example = "PENDING")
        String status,

        @Schema(description = "Created at")
        LocalDateTime createdAt,

        @Schema(description = "Updated at")
        LocalDateTime updatedAt
) {}
