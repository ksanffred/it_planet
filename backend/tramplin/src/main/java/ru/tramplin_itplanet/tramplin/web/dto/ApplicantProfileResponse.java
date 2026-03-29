package ru.tramplin_itplanet.tramplin.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Applicant profile")
public record ApplicantProfileResponse(
        @Schema(description = "Applicant profile id", example = "1")
        Long id,

        @Schema(description = "Related user id", example = "12")
        Long userId,

        @Schema(description = "Applicant name (same as username/display name)", example = "Ivan Ivanov")
        String name,

        @Schema(description = "University", example = "National Research University")
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

        @Schema(description = "Additional education details")
        String additionalEducationDetails,

        @Schema(description = "Portfolio link")
        String portfolioUrl,

        @Schema(description = "Avatar image object path or public URL")
        String avatarUrl,

        @Schema(description = "Resume PDF object path or public URL")
        String resumeUrl,

        @Schema(description = "Applicant favorites visibility status", example = "PRIVATE")
        String visibility,

        @Schema(description = "Skill tags")
        List<TagResponse> skills
) {}
