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
import ru.tramplin_itplanet.tramplin.domain.model.OpportunityResponse;
import ru.tramplin_itplanet.tramplin.domain.model.OpportunityResponseStatus;
import ru.tramplin_itplanet.tramplin.domain.service.OpportunityResponseService;

import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
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
}
