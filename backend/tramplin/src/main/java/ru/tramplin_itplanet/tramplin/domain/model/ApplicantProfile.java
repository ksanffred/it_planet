package ru.tramplin_itplanet.tramplin.domain.model;

import java.io.Serializable;
import java.util.List;

public record ApplicantProfile(
        Long id,
        Long userId,
        String name,
        String university,
        String faculty,
        String currentFieldOfStudy,
        String desiredPosition,
        String major,
        Integer graduationYear,
        String additionalEducationDetails,
        String portfolioUrl,
        String avatarUrl,
        String resumeUrl,
        List<Tag> skills
) implements Serializable {}
