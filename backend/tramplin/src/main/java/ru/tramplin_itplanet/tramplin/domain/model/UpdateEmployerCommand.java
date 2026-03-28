package ru.tramplin_itplanet.tramplin.domain.model;

public record UpdateEmployerCommand(
        String description,
        String website,
        String socials,
        String logoUrl
) {}
