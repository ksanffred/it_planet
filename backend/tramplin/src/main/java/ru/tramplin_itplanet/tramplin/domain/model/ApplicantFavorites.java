package ru.tramplin_itplanet.tramplin.domain.model;

import java.util.List;

public record ApplicantFavorites(
        Long applicantId,
        List<Long> opportunityIds
) {}
