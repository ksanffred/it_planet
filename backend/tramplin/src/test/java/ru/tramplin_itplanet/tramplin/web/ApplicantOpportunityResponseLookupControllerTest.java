package ru.tramplin_itplanet.tramplin.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.tramplin_itplanet.tramplin.di.JwtService;
import ru.tramplin_itplanet.tramplin.domain.model.ApplicantOpportunityResponseCard;
import ru.tramplin_itplanet.tramplin.domain.model.OpportunityResponseStatus;
import ru.tramplin_itplanet.tramplin.domain.model.OpportunityStatus;
import ru.tramplin_itplanet.tramplin.domain.model.OpportunityType;
import ru.tramplin_itplanet.tramplin.domain.service.OpportunityResponseService;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApplicantOpportunityResponseLookupController.class)
class ApplicantOpportunityResponseLookupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OpportunityResponseService opportunityResponseService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser(username = "employer@example.com", roles = "EMPLOYER")
    void getResponsesByApplicantId_authorizedViewer_returns200() throws Exception {
        when(opportunityResponseService.getResponsesByApplicantIdForViewer("employer@example.com", 7L))
                .thenReturn(List.of(
                        new ApplicantOpportunityResponseCard(
                                "Java Developer",
                                "Acme Corp",
                                OpportunityResponseStatus.NOT_REVIEWED,
                                OpportunityType.VACANCY,
                                OpportunityStatus.ACTIVE
                        )
                ));

        mockMvc.perform(get("/applicants/7/responses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Java Developer"))
                .andExpect(jsonPath("$[0].company_name").value("Acme Corp"))
                .andExpect(jsonPath("$[0].response_status").value("NOT_REVIEWED"));
    }
}
