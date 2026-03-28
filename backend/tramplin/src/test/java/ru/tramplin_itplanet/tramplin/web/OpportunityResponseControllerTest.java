package ru.tramplin_itplanet.tramplin.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.tramplin_itplanet.tramplin.di.JwtService;
import ru.tramplin_itplanet.tramplin.domain.exception.OpportunityResponseAlreadyExistsException;
import ru.tramplin_itplanet.tramplin.domain.model.ApplicantOpportunityResponseCard;
import ru.tramplin_itplanet.tramplin.domain.model.EmployerOpportunityApplication;
import ru.tramplin_itplanet.tramplin.domain.model.OpportunityStatus;
import ru.tramplin_itplanet.tramplin.domain.model.OpportunityResponse;
import ru.tramplin_itplanet.tramplin.domain.model.OpportunityResponseStatus;
import ru.tramplin_itplanet.tramplin.domain.model.OpportunityType;
import ru.tramplin_itplanet.tramplin.domain.service.OpportunityResponseService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OpportunityResponseController.class)
class OpportunityResponseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OpportunityResponseService opportunityResponseService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser(username = "applicant@example.com", roles = "APPLICANT")
    void apply_validApplicant_returns201() throws Exception {
        when(opportunityResponseService.apply(10L, "applicant@example.com")).thenReturn(
                new OpportunityResponse(
                        1L,
                        10L,
                        3L,
                        OpportunityResponseStatus.NOT_REVIEWED,
                        LocalDateTime.of(2026, 3, 28, 12, 0),
                        LocalDateTime.of(2026, 3, 28, 12, 0)
                )
        );

        mockMvc.perform(post("/opportunities/10/responses"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.opportunityId").value(10))
                .andExpect(jsonPath("$.applicantId").value(3))
                .andExpect(jsonPath("$.status").value("NOT_REVIEWED"));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void apply_nonApplicantRole_returns403() throws Exception {
        mockMvc.perform(post("/opportunities/10/responses"))
                .andExpect(status().isForbidden());
    }

    @Test
    void apply_withoutAuthentication_returns401() throws Exception {
        mockMvc.perform(post("/opportunities/10/responses"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "applicant@example.com", roles = "APPLICANT")
    void apply_duplicateResponse_returns409() throws Exception {
        when(opportunityResponseService.apply(10L, "applicant@example.com"))
                .thenThrow(new OpportunityResponseAlreadyExistsException(10L, 3L));

        mockMvc.perform(post("/opportunities/10/responses"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Response already exists for opportunity id: 10 and applicant id: 3"));
    }

    @Test
    @WithMockUser(username = "applicant@example.com", roles = "APPLICANT")
    void myResponses_validApplicant_returns200() throws Exception {
        when(opportunityResponseService.getMyResponses("applicant@example.com"))
                .thenReturn(List.of(
                        new ApplicantOpportunityResponseCard(
                                "Java Developer",
                                "Acme Corp",
                                OpportunityResponseStatus.NOT_REVIEWED,
                                OpportunityType.VACANCY,
                                OpportunityStatus.ACTIVE
                        )
                ));

        mockMvc.perform(get("/opportunities/responses/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Java Developer"))
                .andExpect(jsonPath("$[0].company_name").value("Acme Corp"))
                .andExpect(jsonPath("$[0].response_status").value("NOT_REVIEWED"))
                .andExpect(jsonPath("$[0].opportunity_type").value("VACANCY"))
                .andExpect(jsonPath("$[0].opportunity_status").value("ACTIVE"));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void myResponses_nonApplicantRole_returns403() throws Exception {
        mockMvc.perform(get("/opportunities/responses/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    void myResponses_withoutAuthentication_returns401() throws Exception {
        mockMvc.perform(get("/opportunities/responses/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "employer@example.com", roles = "EMPLOYER")
    void applicationsByOpportunity_employer_returns200() throws Exception {
        when(opportunityResponseService.getApplicationsForOpportunity(10L, "employer@example.com"))
                .thenReturn(List.of(
                        new EmployerOpportunityApplication(
                                15L,
                                10L,
                                "Java Developer",
                                "Acme Corp",
                                OpportunityType.VACANCY,
                                OpportunityStatus.ACTIVE,
                                3L,
                                "Ivan Ivanov",
                                OpportunityResponseStatus.NOT_REVIEWED,
                                LocalDateTime.of(2026, 3, 28, 14, 0)
                        )
                ));

        mockMvc.perform(get("/opportunities/10/responses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].responseId").value(15))
                .andExpect(jsonPath("$[0].opportunityId").value(10))
                .andExpect(jsonPath("$[0].title").value("Java Developer"))
                .andExpect(jsonPath("$[0].company_name").value("Acme Corp"))
                .andExpect(jsonPath("$[0].response_status").value("NOT_REVIEWED"))
                .andExpect(jsonPath("$[0].opportunity_type").value("VACANCY"))
                .andExpect(jsonPath("$[0].opportunity_status").value("ACTIVE"))
                .andExpect(jsonPath("$[0].applicant_id").value(3))
                .andExpect(jsonPath("$[0].applicant_name").value("Ivan Ivanov"));
    }

    @Test
    @WithMockUser(username = "employer@example.com", roles = "EMPLOYER")
    void applicationsForEmployer_employer_returns200() throws Exception {
        when(opportunityResponseService.getApplicationsForMyOpportunities("employer@example.com"))
                .thenReturn(List.of(
                        new EmployerOpportunityApplication(
                                15L,
                                10L,
                                "Java Developer",
                                "Acme Corp",
                                OpportunityType.VACANCY,
                                OpportunityStatus.ACTIVE,
                                3L,
                                "Ivan Ivanov",
                                OpportunityResponseStatus.NOT_REVIEWED,
                                LocalDateTime.of(2026, 3, 28, 14, 0)
                        )
                ));

        mockMvc.perform(get("/opportunities/responses/employer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].responseId").value(15))
                .andExpect(jsonPath("$[0].company_name").value("Acme Corp"))
                .andExpect(jsonPath("$[0].applicant_name").value("Ivan Ivanov"));
    }

    @Test
    @WithMockUser(username = "applicant@example.com", roles = "APPLICANT")
    void applicationsForEmployer_applicantRole_returns403() throws Exception {
        mockMvc.perform(get("/opportunities/responses/employer"))
                .andExpect(status().isForbidden());
    }
}
