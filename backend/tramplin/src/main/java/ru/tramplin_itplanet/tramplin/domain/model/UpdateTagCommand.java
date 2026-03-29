package ru.tramplin_itplanet.tramplin.domain.model;

public record UpdateTagCommand(
        String name,
        TagCategory category
) {}
