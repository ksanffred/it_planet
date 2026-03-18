package ru.tramplin_itplanet.tramplin.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.tramplin_itplanet.tramplin.domain.model.UserRole;

@Schema(description = "User registration request")
public record RegisterRequest(
        @Schema(description = "Email address", example = "user@example.com")
        @NotBlank @Email String email,

        @Schema(description = "Display name shown on the platform", example = "Ivan Ivanov")
        @NotBlank String displayName,

        @Schema(description = "Password (min 8 characters)", example = "securePass123")
        @NotBlank @Size(min = 8) String password,

        @Schema(description = "User role", allowableValues = {"USER", "EMPLOYER", "ADMIN"})
        @NotNull UserRole role
) {}
