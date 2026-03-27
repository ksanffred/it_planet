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
import ru.tramplin_itplanet.tramplin.domain.exception.TagAlreadyExistsException;
import ru.tramplin_itplanet.tramplin.domain.model.Tag;
import ru.tramplin_itplanet.tramplin.domain.model.TagCategory;
import ru.tramplin_itplanet.tramplin.domain.service.TagService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TagController.class)
class TagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TagService tagService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    void getAll_returns200WithTags() throws Exception {
        when(tagService.findAll()).thenReturn(List.of(
                new Tag(1L, "Java", TagCategory.TECHNOLOGY),
                new Tag(2L, "Middle", TagCategory.LEVEL)
        ));

        mockMvc.perform(get("/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Java"))
                .andExpect(jsonPath("$[0].category").value("TECHNOLOGY"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Middle"))
                .andExpect(jsonPath("$[1].category").value("LEVEL"));
    }

    @Test
    @WithMockUser
    void create_validRequest_returns201WithTag() throws Exception {
        when(tagService.create(any())).thenReturn(new Tag(1L, "Docker", TagCategory.TECHNOLOGY));

        mockMvc.perform(post("/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Docker",
                                  "category": "TECHNOLOGY"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Docker"))
                .andExpect(jsonPath("$.category").value("TECHNOLOGY"));
    }

    @Test
    @WithMockUser
    void create_missingRequiredFields_returns400() throws Exception {
        mockMvc.perform(post("/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @WithMockUser
    void create_duplicateName_returns409() throws Exception {
        when(tagService.create(any())).thenThrow(new TagAlreadyExistsException("Docker"));

        mockMvc.perform(post("/tags")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Docker",
                                  "category": "TECHNOLOGY"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Tag already exists with name: Docker"));
    }

}
