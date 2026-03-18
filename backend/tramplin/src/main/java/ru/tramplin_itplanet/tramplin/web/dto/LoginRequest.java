package ru.tramplin_itplanet.tramplin.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Login request")
public record LoginRequest(
        @Schema(description = "Email address", example = "user@example.com")
        @NotBlank @Email String email,

        @Schema(description = "Password", example = "securePass123")
        @NotBlank String password
) {}
