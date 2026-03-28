package ru.tramplin_itplanet.tramplin.web.mapper;

import ru.tramplin_itplanet.tramplin.domain.model.ApplicantProfile;
import ru.tramplin_itplanet.tramplin.domain.model.CreateApplicantCommand;
import ru.tramplin_itplanet.tramplin.web.dto.ApplicantProfileResponse;
import ru.tramplin_itplanet.tramplin.web.dto.CreateApplicantRequest;
import ru.tramplin_itplanet.tramplin.web.dto.TagResponse;

import java.util.List;

public final class ApplicantMapper {

    private ApplicantMapper() {}

    public static CreateApplicantCommand toCommand(CreateApplicantRequest request) {
        return new CreateApplicantCommand(
                request.userId(),
                request.university(),
                request.faculty(),
                request.currentFieldOfStudy(),
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
                profile.major(),
                profile.graduationYear(),
                profile.additionalEducationDetails(),
                profile.portfolioUrl(),
                profile.resumeUrl(),
                profile.skills().stream()
                        .map(tag -> new TagResponse(tag.id(), tag.name(), tag.category().name()))
                        .toList()
        );
    }
}
