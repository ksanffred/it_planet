package ru.tramplin_itplanet.tramplin.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.tramplin_itplanet.tramplin.di.EmailVerificationService;
import ru.tramplin_itplanet.tramplin.di.JwtService;
import ru.tramplin_itplanet.tramplin.domain.exception.InvalidVerificationTokenException;
import ru.tramplin_itplanet.tramplin.domain.service.AuthService;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private EmailVerificationService emailVerificationService;

    @Test
    void verify_validToken_returns200() throws Exception {
        doNothing().when(authService).verifyEmail("valid-token");

        mockMvc.perform(get("/auth/verify").param("token", "valid-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Email verified successfully"));
    }

    @Test
    void verify_invalidToken_returns400() throws Exception {
        doThrow(new InvalidVerificationTokenException()).when(authService).verifyEmail("bad-token");

        mockMvc.perform(get("/auth/verify").param("token", "bad-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Verification token is invalid or has expired"));
    }
}
