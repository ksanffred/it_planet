package ru.tramplin_itplanet.tramplin.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.tramplin_itplanet.tramplin.di.JwtService;
import ru.tramplin_itplanet.tramplin.domain.model.ApplicantOpportunityRecommendation;
import ru.tramplin_itplanet.tramplin.domain.service.ApplicantOpportunityRecommendationService;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApplicantOpportunityRecommendationController.class)
class ApplicantOpportunityRecommendationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApplicantOpportunityRecommendationService recommendationService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser(username = "applicant@example.com", roles = "APPLICANT")
    void create_validApplicant_returns201() throws Exception {
        when(recommendationService.create("applicant@example.com", 10L, 7L)).thenReturn(
                new ApplicantOpportunityRecommendation(
                        1L,
                        3L,
                        7L,
                        10L,
                        LocalDateTime.of(2026, 3, 29, 10, 0)
                )
        );

        mockMvc.perform(post("/opportunities/10/recommendations/7"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.recommenderApplicantId").value(3))
                .andExpect(jsonPath("$.recommendedApplicantId").value(7))
                .andExpect(jsonPath("$.opportunityId").value(10));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void create_nonApplicantRole_returns403() throws Exception {
        mockMvc.perform(post("/opportunities/10/recommendations/7"))
                .andExpect(status().isForbidden());
    }
}
