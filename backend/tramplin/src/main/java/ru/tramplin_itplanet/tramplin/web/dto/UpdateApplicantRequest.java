package ru.tramplin_itplanet.tramplin.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "Request payload for applicant profile update")
public record UpdateApplicantRequest(
        @NotNull
        @Schema(description = "Related user id (must have APPLICANT role)", example = "12")
        Long userId,

        @Schema(description = "Applicant name", example = "Ivan Ivanov")
        String name,

        @Schema(description = "University name", example = "National Research University")
        String university,

        @Schema(description = "Faculty", example = "Faculty of Computer Science")
        String faculty,

        @Schema(description = "Current field of study", example = "Software Engineering")
        String currentFieldOfStudy,

        @Schema(description = "Desired position", example = "Backend Developer Intern")
        String desiredPosition,

        @Schema(description = "Major", example = "Applied Informatics")
        String major,

        @Schema(description = "Graduation year", example = "2027")
        Integer graduationYear,

        @Schema(description = "Important details about additional education")
        String additionalEducationDetails,

        @Schema(description = "Portfolio external link", example = "https://github.com/username")
        String portfolioUrl,

        @Schema(description = "Resume PDF object path or public URL", example = "applicants/15/resume/1234.pdf")
        String resumeUrl,

        @Schema(description = "Skill tags IDs")
        List<Long> skillTagIds
) {}
