package ru.tramplin_itplanet.tramplin.domain.model;

import java.io.Serializable;

public record EmployerProfile(
        Long id,
        Long userId,
        String companyName,
        String description,
        String inn,
        String website,
        String socials,
        String logoUrl,
        String verifiedOrgName,
        String status
) implements Serializable {}
