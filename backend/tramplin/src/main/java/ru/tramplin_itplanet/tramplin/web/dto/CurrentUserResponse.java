package ru.tramplin_itplanet.tramplin.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Current authenticated user response")
public record CurrentUserResponse(
        @Schema(description = "User ID") Long userId,
        @Schema(description = "User email") String email,
        @Schema(description = "Display name") String displayName,
        @Schema(description = "User role", allowableValues = {"USER", "APPLICANT", "EMPLOYER", "ADMIN"}) String role,
        @Schema(description = "Email verification flag") boolean verified
) {}
