package ru.tramplin_itplanet.tramplin.domain.model;

import java.util.List;

public record CreateApplicantCommand(
        Long userId,
        String university,
        String faculty,
        String currentFieldOfStudy,
        String desiredPosition,
        String major,
        Integer graduationYear,
        String additionalEducationDetails,
        String portfolioUrl,
        String resumeUrl,
        List<Long> skillTagIds
) {}
