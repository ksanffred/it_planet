package ru.tramplin_itplanet.tramplin.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.tramplin_itplanet.tramplin.di.JwtService;
import ru.tramplin_itplanet.tramplin.domain.model.ApplicantContact;
import ru.tramplin_itplanet.tramplin.domain.model.ApplicantContactPreview;
import ru.tramplin_itplanet.tramplin.domain.model.ApplicantContactStatus;
import ru.tramplin_itplanet.tramplin.domain.service.ApplicantContactService;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApplicantContactController.class)
class ApplicantContactControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApplicantContactService applicantContactService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser(username = "applicant@example.com", roles = "APPLICANT")
    void create_validApplicant_returns201() throws Exception {
        when(applicantContactService.create("applicant@example.com", 7L)).thenReturn(
                new ApplicantContact(
                        1L,
                        3L,
                        7L,
                        ApplicantContactStatus.PENDING,
                        LocalDateTime.of(2026, 3, 29, 10, 0),
                        LocalDateTime.of(2026, 3, 29, 10, 0)
                )
        );

        mockMvc.perform(post("/applicants/me/contacts/7"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.requesterApplicantId").value(3))
                .andExpect(jsonPath("$.recipientApplicantId").value(7))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "applicant@example.com", roles = "APPLICANT")
    void updateStatus_validApplicant_returns200() throws Exception {
        when(applicantContactService.updateStatus("applicant@example.com", 1L, ApplicantContactStatus.ACCEPTED)).thenReturn(
                new ApplicantContact(
                        1L,
                        3L,
                        7L,
                        ApplicantContactStatus.ACCEPTED,
                        LocalDateTime.of(2026, 3, 29, 10, 0),
                        LocalDateTime.of(2026, 3, 29, 10, 5)
                )
        );

        mockMvc.perform(put("/applicants/me/contacts/1/status")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "ACCEPTED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void create_nonApplicantRole_returns403() throws Exception {
        mockMvc.perform(post("/applicants/me/contacts/7"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "applicant@example.com", roles = "APPLICANT")
    void getMyContacts_validApplicant_returns200() throws Exception {
        when(applicantContactService.getMyContacts("applicant@example.com")).thenReturn(
                java.util.List.of(new ApplicantContactPreview(
                        "https://cdn.example.com/photos/user1.jpg",
                        "Ivan Ivanov",
                        "Backend Developer Intern",
                        "received"
                ))
        );

        mockMvc.perform(get("/applicants/me/contacts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].photo").value("https://cdn.example.com/photos/user1.jpg"))
                .andExpect(jsonPath("$[0].name").value("Ivan Ivanov"))
                .andExpect(jsonPath("$[0].desired_profession").value("Backend Developer Intern"))
                .andExpect(jsonPath("$[0].status").value("received"));
    }
}
