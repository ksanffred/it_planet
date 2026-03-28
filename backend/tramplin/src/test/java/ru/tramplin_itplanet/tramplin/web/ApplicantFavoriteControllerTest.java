package ru.tramplin_itplanet.tramplin.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.tramplin_itplanet.tramplin.di.JwtService;
import ru.tramplin_itplanet.tramplin.domain.model.ApplicantFavorites;
import ru.tramplin_itplanet.tramplin.domain.service.ApplicantFavoriteService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApplicantFavoriteController.class)
class ApplicantFavoriteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApplicantFavoriteService applicantFavoriteService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser(username = "applicant@example.com", roles = "APPLICANT")
    void addOne_validApplicant_returns200() throws Exception {
        when(applicantFavoriteService.addOneByUserEmail("applicant@example.com", 10L))
                .thenReturn(new ApplicantFavorites(1L, List.of(10L, 5L)));

        mockMvc.perform(post("/applicants/me/favorites/opportunities/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicantId").value(1))
                .andExpect(jsonPath("$.opportunityIds[0]").value(10))
                .andExpect(jsonPath("$.opportunityIds[1]").value(5));
    }

    @Test
    @WithMockUser(username = "applicant@example.com", roles = "APPLICANT")
    void addMany_validApplicant_returns200() throws Exception {
        when(applicantFavoriteService.addManyByUserEmail(eq("applicant@example.com"), any()))
                .thenReturn(new ApplicantFavorites(1L, List.of(12L, 10L, 5L)));

        mockMvc.perform(post("/applicants/me/favorites/opportunities/bulk")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "opportunityIds": [12, 10, 5]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.applicantId").value(1))
                .andExpect(jsonPath("$.opportunityIds.length()").value(3))
                .andExpect(jsonPath("$.opportunityIds[0]").value(12));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void addMany_nonApplicantRole_returns403() throws Exception {
        mockMvc.perform(post("/applicants/me/favorites/opportunities/bulk")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "opportunityIds": [12]
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void addMany_withoutAuthentication_returns401() throws Exception {
        mockMvc.perform(post("/applicants/me/favorites/opportunities/bulk")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "opportunityIds": [12]
                                }
                                """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "applicant@example.com", roles = "APPLICANT")
    void addMany_emptyList_returns400() throws Exception {
        mockMvc.perform(post("/applicants/me/favorites/opportunities/bulk")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "opportunityIds": []
                                }
                                """))
                .andExpect(status().isBadRequest());
    }
}
