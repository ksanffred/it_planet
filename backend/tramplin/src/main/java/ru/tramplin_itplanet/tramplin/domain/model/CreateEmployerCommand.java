package ru.tramplin_itplanet.tramplin.domain.model;

public record CreateEmployerCommand(
        Long userId,
        String companyName,
        String description,
        String inn,
        String website,
        String socials,
        String logoUrl
) {}
