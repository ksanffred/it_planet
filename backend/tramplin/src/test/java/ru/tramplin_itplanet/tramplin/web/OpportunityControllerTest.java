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
import ru.tramplin_itplanet.tramplin.domain.exception.OpportunityNotFoundException;
import ru.tramplin_itplanet.tramplin.domain.model.*;
import ru.tramplin_itplanet.tramplin.domain.service.OpportunityService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OpportunityController.class)
class OpportunityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OpportunityService opportunityService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void getCard_existingId_returns200WithOpportunityCard() throws Exception {
        when(opportunityService.getById(1L)).thenReturn(buildOpportunity(1L));

        mockMvc.perform(get("/opportunities/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Java Developer"))
                .andExpect(jsonPath("$.type").value("VACANCY"))
                .andExpect(jsonPath("$.employer.name").value("Acme Corp"));
    }

    @Test
    void getCard_nonExistingId_returns404() throws Exception {
        when(opportunityService.getById(99L)).thenThrow(new OpportunityNotFoundException(99L));

        mockMvc.perform(get("/opportunities/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Opportunity not found with id: 99"));
    }

    @Test
    @WithMockUser
    void create_validRequest_returns201WithCreatedCard() throws Exception {
        when(opportunityService.create(any())).thenReturn(buildOpportunity(1L));

        mockMvc.perform(post("/opportunities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "employerId": 1,
                                  "title": "Java Developer",
                                  "type": "VACANCY",
                                  "format": "REMOTE",
                                  "status": "ACTIVE"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Java Developer"))
                .andExpect(jsonPath("$.type").value("VACANCY"));
    }

    @Test
    @WithMockUser
    void create_missingRequiredFields_returns400() throws Exception {
        mockMvc.perform(post("/opportunities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @WithMockUser
    void create_unknownEmployerId_returns404() throws Exception {
        when(opportunityService.create(any())).thenThrow(new EmployerNotFoundException(99L));

        mockMvc.perform(post("/opportunities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "employerId": 99,
                                  "title": "Java Developer",
                                  "type": "VACANCY",
                                  "format": "REMOTE",
                                  "status": "ACTIVE"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Employer not found with id: 99"));
    }

    private Opportunity buildOpportunity(Long id) {
        return new Opportunity(
                id,
                new Employer(1L, "Acme Corp", null, "https://acme.com", "hr@acme.com"),
                "Java Developer",
                "Backend role",
                OpportunityType.VACANCY,
                OpportunityFormat.REMOTE,
                null,
                "Moscow",
                null,
                null,
                BigDecimal.valueOf(100_000),
                BigDecimal.valueOf(150_000),
                LocalDateTime.of(2026, 1, 1, 0, 0),
                LocalDateTime.of(2026, 6, 1, 0, 0),
                OpportunityStatus.ACTIVE,
                List.of(),
                List.of()
        );
    }
}
