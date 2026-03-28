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
import ru.tramplin_itplanet.tramplin.domain.service.EmployerService;
import ru.tramplin_itplanet.tramplin.domain.service.OpportunityService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OpportunityController.class)
class OpportunityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OpportunityService opportunityService;

    @MockitoBean
    private EmployerService employerService;

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
    void getMiniCards_returns200WithMiniCardFields() throws Exception {
        when(opportunityService.findActiveMiniCards(null)).thenReturn(List.of(
                new OpportunityMiniCard(
                        1L,
                        "https://cdn.tramplin.ru/media/1.png",
                        "Java Developer",
                        "Backend role",
                        "Acme Corp",
                        "REMOTE",
                        List.of("Java", "Spring", "Docker")
                )
        ));

        mockMvc.perform(get("/opportunities/mini-cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].media").value("https://cdn.tramplin.ru/media/1.png"))
                .andExpect(jsonPath("$[0].title").value("Java Developer"))
                .andExpect(jsonPath("$[0].description").value("Backend role"))
                .andExpect(jsonPath("$[0].employerName").value("Acme Corp"))
                .andExpect(jsonPath("$[0].format").value("REMOTE"))
                .andExpect(jsonPath("$[0].tags.length()").value(3))
                .andExpect(jsonPath("$[0].tags[0]").value("Java"))
                .andExpect(jsonPath("$[0].tags[1]").value("Spring"))
                .andExpect(jsonPath("$[0].tags[2]").value("Docker"));
    }

    @Test
    void getMiniCards_withSearch_returns200WithFilteredMiniCards() throws Exception {
        when(opportunityService.findActiveMiniCards("java")).thenReturn(List.of(
                new OpportunityMiniCard(
                        2L,
                        "https://cdn.tramplin.ru/media/2.png",
                        "Java Intern",
                        "Internship for backend team",
                        "Beta Corp",
                        "REMOTE",
                        List.of("Java", "Intern", "Spring")
                )
        ));

        mockMvc.perform(get("/opportunities/mini-cards").param("search", "java"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].title").value("Java Intern"))
                .andExpect(jsonPath("$[0].employerName").value("Beta Corp"));
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
    @WithMockUser(username = "employer@example.com", roles = "EMPLOYER")
    void create_validRequest_returns201WithCreatedCard() throws Exception {
        doNothing().when(employerService).assertCanManageOpportunities("employer@example.com", 1L);
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
    @WithMockUser(username = "employer@example.com", roles = "EMPLOYER")
    void create_missingRequiredFields_returns400() throws Exception {
        mockMvc.perform(post("/opportunities")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @WithMockUser(username = "employer@example.com", roles = "EMPLOYER")
    void create_unknownEmployerId_returns404() throws Exception {
        doNothing().when(employerService).assertCanManageOpportunities("employer@example.com", 99L);
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

    @Test
    @WithMockUser(username = "employer@example.com", roles = "EMPLOYER")
    void update_validRequest_returns200WithUpdatedCard() throws Exception {
        doNothing().when(employerService).assertCanManageOpportunities("employer@example.com", 1L);
        when(opportunityService.update(any(), any())).thenReturn(buildOpportunity(1L));

        mockMvc.perform(put("/opportunities/1")
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
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Java Developer"))
                .andExpect(jsonPath("$.type").value("VACANCY"));
    }

    @Test
    @WithMockUser(username = "employer@example.com", roles = "EMPLOYER")
    void update_missingRequiredFields_returns400() throws Exception {
        mockMvc.perform(put("/opportunities/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @WithMockUser(username = "employer@example.com", roles = "EMPLOYER")
    void update_nonExistingOpportunity_returns404() throws Exception {
        doNothing().when(employerService).assertCanManageOpportunities("employer@example.com", 1L);
        when(opportunityService.update(any(), any())).thenThrow(new OpportunityNotFoundException(99L));

        mockMvc.perform(put("/opportunities/99")
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
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Opportunity not found with id: 99"));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    void create_nonEmployerRole_returns403() throws Exception {
        doThrow(new org.springframework.security.access.AccessDeniedException("Only EMPLOYER users can manage opportunities"))
                .when(employerService).assertCanManageOpportunities(eq("user@example.com"), eq(1L));

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
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "employer@example.com", roles = "EMPLOYER")
    void update_notFullVerifiedEmployer_returns403() throws Exception {
        doThrow(new org.springframework.security.access.AccessDeniedException("Employer must have full_verified status"))
                .when(employerService).assertCanManageOpportunities(eq("employer@example.com"), eq(1L));

        mockMvc.perform(put("/opportunities/1")
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
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Employer must have full_verified status"));
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
