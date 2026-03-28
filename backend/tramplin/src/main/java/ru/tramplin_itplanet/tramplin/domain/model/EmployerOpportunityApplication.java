package ru.tramplin_itplanet.tramplin.domain.model;

import java.util.List;

public record EmployerOpportunityApplication(
        Long applicantId,
        String applicantName,
        String university,
        String desiredPosition,
        List<Tag> matchingTags
) {}
