package ru.tramplin_itplanet.tramplin.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.tramplin_itplanet.tramplin.di.JwtService;
import ru.tramplin_itplanet.tramplin.domain.model.Curator;
import ru.tramplin_itplanet.tramplin.domain.service.CuratorService;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CuratorController.class)
class CuratorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CuratorService curatorService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser(username = "root@example.com", roles = "CURATOR")
    void create_rootCurator_returns201() throws Exception {
        when(curatorService.create("root@example.com", 42L)).thenReturn(new Curator(2L, 42L));

        mockMvc.perform(post("/curators")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 42
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.userId").value(42));
    }

    @Test
    @WithMockUser(username = "root@example.com", roles = "CURATOR")
    void delete_rootCurator_returns204() throws Exception {
        mockMvc.perform(delete("/curators/2"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void create_nonCuratorRole_returns403() throws Exception {
        mockMvc.perform(post("/curators")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "userId": 42
                                }
                                """))
                .andExpect(status().isForbidden());
    }
}
