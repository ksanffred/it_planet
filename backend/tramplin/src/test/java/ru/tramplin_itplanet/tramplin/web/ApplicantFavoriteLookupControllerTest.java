package ru.tramplin_itplanet.tramplin.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.tramplin_itplanet.tramplin.di.JwtService;
import ru.tramplin_itplanet.tramplin.domain.model.ApplicantFavoriteOpportunityCard;
import ru.tramplin_itplanet.tramplin.domain.model.OpportunityStatus;
import ru.tramplin_itplanet.tramplin.domain.model.OpportunityType;
import ru.tramplin_itplanet.tramplin.domain.service.ApplicantFavoriteService;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApplicantFavoriteLookupController.class)
class ApplicantFavoriteLookupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApplicantFavoriteService applicantFavoriteService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser(username = "employer@example.com", roles = "EMPLOYER")
    void getCardsByApplicantId_authorizedViewer_returns200() throws Exception {
        when(applicantFavoriteService.getCardsByApplicantIdForViewer("employer@example.com", 7L))
                .thenReturn(List.of(
                        new ApplicantFavoriteOpportunityCard(
                                "Java Developer",
                                "Acme Corp",
                                OpportunityStatus.ACTIVE,
                                OpportunityType.VACANCY
                        )
                ));

        mockMvc.perform(get("/applicants/7/favorites/opportunities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Java Developer"))
                .andExpect(jsonPath("$[0].company_name").value("Acme Corp"));
    }
}
