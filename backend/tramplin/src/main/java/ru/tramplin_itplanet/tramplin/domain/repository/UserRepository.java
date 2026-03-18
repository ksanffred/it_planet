package ru.tramplin_itplanet.tramplin.domain.repository;

import ru.tramplin_itplanet.tramplin.domain.model.User;
import ru.tramplin_itplanet.tramplin.domain.model.UserRole;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findByEmail(String email);
    User save(String email, String displayName, String passwordHash, UserRole role);
}
