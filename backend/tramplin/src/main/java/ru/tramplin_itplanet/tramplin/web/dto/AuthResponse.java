package ru.tramplin_itplanet.tramplin.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "JWT authentication response")
public record AuthResponse(
        @Schema(description = "JWT bearer token") String token,
        @Schema(description = "User ID") Long userId,
        @Schema(description = "User email") String email,
        @Schema(description = "Display name") String displayName,
        @Schema(description = "User role", allowableValues = {"USER", "APPLICANT", "EMPLOYER", "ADMIN"}) String role
) {}
 
