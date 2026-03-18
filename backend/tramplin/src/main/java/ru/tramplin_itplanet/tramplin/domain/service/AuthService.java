package ru.tramplin_itplanet.tramplin.domain.service;

import ru.tramplin_itplanet.tramplin.domain.model.UserRole;
import ru.tramplin_itplanet.tramplin.web.dto.AuthResponse;

public interface AuthService {
    AuthResponse register(String email, String displayName, String password, UserRole role);
    AuthResponse login(String email, String password);
    void verifyEmail(String token);
}
