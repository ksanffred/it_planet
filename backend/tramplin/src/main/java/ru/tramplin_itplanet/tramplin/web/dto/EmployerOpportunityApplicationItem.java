package ru.tramplin_itplanet.tramplin.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Application item for employer opportunity")
public record EmployerOpportunityApplicationItem(
        @JsonProperty("applicant_id")
        @Schema(description = "Applicant id", example = "3")
        Long applicantId,

        @JsonProperty("applicant_name")
        @Schema(description = "Applicant name", example = "Ivan Ivanov")
        String applicantName,

        @Schema(description = "University", example = "National Research University")
        String university,

        @JsonProperty("desired_position")
        @Schema(description = "Desired position", example = "Backend Developer Intern")
        String desiredPosition,

        @Schema(description = "Count of recommendations received for this applicant on this opportunity", example = "2")
        long recommendation,

        @JsonProperty("matching_tags")
        @Schema(description = "Tags that are present both in opportunity and applicant profile")
        List<TagResponse> matchingTags
) {}
