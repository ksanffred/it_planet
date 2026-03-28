package ru.tramplin_itplanet.tramplin.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.tramplin_itplanet.tramplin.di.JwtService;
import ru.tramplin_itplanet.tramplin.domain.exception.EmployerNotFoundException;
import ru.tramplin_itplanet.tramplin.domain.exception.UserNotFoundException;
import ru.tramplin_itplanet.tramplin.domain.model.CreateEmployerCommand;
import ru.tramplin_itplanet.tramplin.domain.model.EmployerProfile;
import ru.tramplin_itplanet.tramplin.domain.service.EmployerService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployerController.class)
class EmployerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EmployerService employerService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void register_validRequest_returns201() throws Exception {
        when(employerService.register(any(CreateEmployerCommand.class))).thenReturn(buildEmployer(1L));

        mockMvc.perform(post("/employers/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "inn": "7701234567",
                                  "companyName": "Acme Corp"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.inn").value("7701234567"))
                .andExpect(jsonPath("$.status").value("pending"));
    }

    @Test
    void register_missingInn_returns400() throws Exception {
        mockMvc.perform(post("/employers/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "companyName": "Acme Corp"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void register_missingCompanyName_returns400() throws Exception {
        mockMvc.perform(post("/employers/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "inn": "7701234567"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void register_unknownUser_returns404() throws Exception {
        when(employerService.register(any(CreateEmployerCommand.class))).thenThrow(new UserNotFoundException(999L));

        mockMvc.perform(post("/employers/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 999,
                                  "companyName": "Acme Corp",
                                  "inn": "7701234567"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found with id: 999"));
    }

    @Test
    void getById_existingId_returns200() throws Exception {
        when(employerService.getById(1L)).thenReturn(buildEmployer(1L));

        mockMvc.perform(get("/employers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.companyName").value("Acme Corp"))
                .andExpect(jsonPath("$.inn").value("7701234567"));
    }

    @Test
    void getById_unknownId_returns404() throws Exception {
        when(employerService.getById(999L)).thenThrow(new EmployerNotFoundException(999L));

        mockMvc.perform(get("/employers/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Employer not found with id: 999"));
    }

    @Test
    @WithMockUser(username = "employer@example.com")
    void getCurrent_authenticatedUser_returns200() throws Exception {
        when(employerService.getCurrentByUserEmail("employer@example.com")).thenReturn(buildEmployer(1L));

        mockMvc.perform(get("/employers/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.companyName").value("Acme Corp"))
                .andExpect(jsonPath("$.inn").value("7701234567"));
    }

    @Test
    void getCurrent_withoutAuthentication_returns401() throws Exception {
        mockMvc.perform(get("/employers/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "employer@example.com", roles = "EMPLOYER")
    void updateCurrent_validRequest_returns200() throws Exception {
        when(employerService.updateCurrentByUserEmail(any(), any())).thenReturn(buildEmployer(1L));

        mockMvc.perform(put("/employers/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "Global software company",
                                  "website": "https://acme.com",
                                  "socials": "@acme_hr",
                                  "logoUrl": "https://acme.com/logo.png"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.companyName").value("Acme Corp"))
                .andExpect(jsonPath("$.inn").value("7701234567"));
    }

    @Test
    @WithMockUser(username = "employer@example.com")
    void updateCurrent_nonEmployerRole_returns403() throws Exception {
        mockMvc.perform(put("/employers/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "Global software company"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    private EmployerProfile buildEmployer(Long id) {
        return new EmployerProfile(
                id,
                null,
                "Acme Corp",
                null,
                "7701234567",
                "https://acme.com",
                "@acme_hr",
                null,
                "Acme Corp",
                "pending"
        );
    }
}
