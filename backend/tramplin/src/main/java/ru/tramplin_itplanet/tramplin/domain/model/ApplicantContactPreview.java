package ru.tramplin_itplanet.tramplin.domain.model;

public record ApplicantContactPreview(
        Long id,
        String photo,
        String name,
        String desiredProfession,
        String status
) {}
