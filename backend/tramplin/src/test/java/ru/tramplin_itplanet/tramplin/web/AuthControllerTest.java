package ru.tramplin_itplanet.tramplin.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.tramplin_itplanet.tramplin.di.EmailVerificationService;
import ru.tramplin_itplanet.tramplin.di.JwtService;
import ru.tramplin_itplanet.tramplin.domain.exception.InvalidVerificationTokenException;
import ru.tramplin_itplanet.tramplin.domain.model.UserRole;
import ru.tramplin_itplanet.tramplin.domain.service.AuthService;
import ru.tramplin_itplanet.tramplin.web.dto.AuthResponse;
import ru.tramplin_itplanet.tramplin.web.dto.CurrentUserResponse;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
    @WithMockUser(username = "user@example.com")
    void me_authenticatedUser_returns200WithCurrentUser() throws Exception {
        when(authService.getCurrentUser("user@example.com"))
                .thenReturn(new CurrentUserResponse(
                        1L,
                        "user@example.com",
                        "User Name",
                        "USER",
                        true
                ));

        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.displayName").value("User Name"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.verified").value(true));
    }

    @Test
    void me_withoutAuthentication_returns401() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());
    }

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

    @Test
    void register_withoutRole_defaultsToApplicant() throws Exception {
        when(authService.register("user@example.com", "Ivan Ivanov", "securePass123", UserRole.APPLICANT))
                .thenReturn(new AuthResponse("token", 1L, "user@example.com", "Ivan Ivanov", "APPLICANT"));

        mockMvc.perform(post("/auth/register")
                        .contentType("application/json")
                        .content("""
                                {
                                  "email": "user@example.com",
                                  "displayName": "Ivan Ivanov",
                                  "password": "securePass123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("APPLICANT"));
    }
}
