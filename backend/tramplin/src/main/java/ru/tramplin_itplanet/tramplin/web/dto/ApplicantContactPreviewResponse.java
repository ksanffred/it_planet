package ru.tramplin_itplanet.tramplin.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Applicant contact preview item")
public record ApplicantContactPreviewResponse(
        @Schema(description = "Contact photo URL", nullable = true, example = "https://cdn.example.com/photos/user1.jpg")
        String photo,

        @Schema(description = "Contact name", example = "Ivan Ivanov")
        String name,

        @JsonProperty("desired_profession")
        @Schema(description = "Desired profession", example = "Backend Developer Intern")
        String desiredProfession
) {}
