package ru.tramplin_itplanet.tramplin.web.mapper;

import ru.tramplin_itplanet.tramplin.domain.model.ApplicantProfile;
import ru.tramplin_itplanet.tramplin.domain.model.CreateApplicantCommand;
import ru.tramplin_itplanet.tramplin.domain.model.UpdateCurrentApplicantCommand;
import ru.tramplin_itplanet.tramplin.domain.model.UpdateApplicantCommand;
import ru.tramplin_itplanet.tramplin.web.dto.ApplicantProfileResponse;
import ru.tramplin_itplanet.tramplin.web.dto.CreateApplicantRequest;
import ru.tramplin_itplanet.tramplin.web.dto.TagResponse;
import ru.tramplin_itplanet.tramplin.web.dto.UpdateCurrentApplicantRequest;
import ru.tramplin_itplanet.tramplin.web.dto.UpdateApplicantRequest;

import java.util.List;

public final class ApplicantMapper {

    private ApplicantMapper() {}

    public static CreateApplicantCommand toCommand(CreateApplicantRequest request) {
        return new CreateApplicantCommand(
                request.userId(),
                request.university(),
                request.faculty(),
                request.currentFieldOfStudy(),
                request.desiredPosition(),
                request.major(),
                request.graduationYear(),
                request.additionalEducationDetails(),
                request.portfolioUrl(),
                request.resumeUrl(),
                request.skillTagIds() != null ? request.skillTagIds() : List.of()
        );
    }

    public static ApplicantProfileResponse toResponse(ApplicantProfile profile) {
        return new ApplicantProfileResponse(
                profile.id(),
                profile.userId(),
                profile.name(),
                profile.university(),
                profile.faculty(),
                profile.currentFieldOfStudy(),
                profile.desiredPosition(),
                profile.major(),
                profile.graduationYear(),
                profile.additionalEducationDetails(),
                profile.portfolioUrl(),
                profile.avatarUrl(),
                profile.resumeUrl(),
                profile.visibility().name(),
                profile.skills().stream()
                        .map(tag -> new TagResponse(tag.id(), tag.name(), tag.category().name()))
                        .toList()
        );
    }

    public static UpdateApplicantCommand toCommand(UpdateApplicantRequest request) {
        return new UpdateApplicantCommand(
                request.userId(),
                request.name(),
                request.university(),
                request.faculty(),
                request.currentFieldOfStudy(),
                request.desiredPosition(),
                request.major(),
                request.graduationYear(),
                request.additionalEducationDetails(),
                request.portfolioUrl(),
                request.resumeUrl(),
                request.skillTagIds() != null ? request.skillTagIds() : List.of()
        );
    }

    public static UpdateCurrentApplicantCommand toCommand(UpdateCurrentApplicantRequest request) {
        return new UpdateCurrentApplicantCommand(
                request.name(),
                request.university(),
                request.faculty(),
                request.currentFieldOfStudy(),
                request.desiredPosition(),
                request.major(),
                request.graduationYear(),
                request.additionalEducationDetails(),
                request.portfolioUrl(),
                request.resumeUrl(),
                request.skillTagIds() != null ? request.skillTagIds() : List.of()
        );
    }
}
