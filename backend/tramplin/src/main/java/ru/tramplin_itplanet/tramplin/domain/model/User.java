package ru.tramplin_itplanet.tramplin.domain.model;

import java.time.LocalDateTime;

public record User(
        Long id,
        String email,
        String displayName,
        String passwordHash,
        UserRole role,
        boolean isVerified,
        LocalDateTime createdAt
) {}
