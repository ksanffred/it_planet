package ru.tramplin_itplanet.tramplin.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.tramplin_itplanet.tramplin.di.JwtService;
import ru.tramplin_itplanet.tramplin.domain.exception.ApplicantNotFoundException;
import ru.tramplin_itplanet.tramplin.domain.model.ApplicantProfile;
import ru.tramplin_itplanet.tramplin.domain.model.Tag;
import ru.tramplin_itplanet.tramplin.domain.model.TagCategory;
import ru.tramplin_itplanet.tramplin.domain.service.ApplicantService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApplicantController.class)
class ApplicantControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ApplicantService applicantService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser(roles = "APPLICANT")
    void create_validRequest_returns201() throws Exception {
        when(applicantService.create(any())).thenReturn(buildProfile(1L));

        mockMvc.perform(post("/applicants")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 12,
                                  "university": "RANEPA",
                                  "faculty": "IT",
                                  "currentFieldOfStudy": "Software Engineering",
                                  "major": "Applied Informatics",
                                  "graduationYear": 2027,
                                  "additionalEducationDetails": "ML course",
                                  "portfolioUrl": "https://github.com/user",
                                  "resumeUrl": "applicants/1/resume/cv.pdf",
                                  "skillTagIds": [1, 2]
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Ivan Ivanov"))
                .andExpect(jsonPath("$.skills[0].name").value("Java"));
    }

    @Test
    @WithMockUser
    void getById_existingProfile_returns200() throws Exception {
        when(applicantService.getById(1L)).thenReturn(buildProfile(1L));

        mockMvc.perform(get("/applicants/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(12))
                .andExpect(jsonPath("$.name").value("Ivan Ivanov"));
    }

    @Test
    @WithMockUser
    void getById_missingProfile_returns404() throws Exception {
        when(applicantService.getById(999L)).thenThrow(new ApplicantNotFoundException(999L));

        mockMvc.perform(get("/applicants/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Applicant not found with id: 999"));
    }

    private ApplicantProfile buildProfile(Long id) {
        return new ApplicantProfile(
                id,
                12L,
                "Ivan Ivanov",
                "RANEPA",
                "IT",
                "Software Engineering",
                "Applied Informatics",
                2027,
                "ML course",
                "https://github.com/user",
                "applicants/1/resume/cv.pdf",
                List.of(
                        new Tag(2L, "Java", TagCategory.TECHNOLOGY),
                        new Tag(10L, "Spring Boot", TagCategory.TECHNOLOGY)
                )
        );
    }
}
