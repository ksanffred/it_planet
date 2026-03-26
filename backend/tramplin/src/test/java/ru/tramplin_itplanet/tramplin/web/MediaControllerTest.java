package ru.tramplin_itplanet.tramplin.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.tramplin_itplanet.tramplin.di.JwtService;
import ru.tramplin_itplanet.tramplin.domain.exception.EmployerNotFoundException;
import ru.tramplin_itplanet.tramplin.domain.exception.InvalidFileException;
import ru.tramplin_itplanet.tramplin.domain.service.MediaService;
import ru.tramplin_itplanet.tramplin.web.dto.MediaUploadResponse;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MediaController.class)
class MediaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MediaService mediaService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser
    void uploadEmployerLogo_validImage_returns201() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "logo.png", MediaType.IMAGE_PNG_VALUE, "img".getBytes());
        when(mediaService.uploadEmployerLogo(1L, file))
                .thenReturn(new MediaUploadResponse("employers/1/test.png", "https://cdn.test/employers/1/test.png"));

        mockMvc.perform(multipart("/employers/1/logo").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.path").value("employers/1/test.png"));
    }

    @Test
    @WithMockUser
    void uploadEmployerLogo_unknownEmployer_returns404() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "logo.png", MediaType.IMAGE_PNG_VALUE, "img".getBytes());
        when(mediaService.uploadEmployerLogo(999L, file))
                .thenThrow(new EmployerNotFoundException(999L));

        mockMvc.perform(multipart("/employers/999/logo").file(file))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Employer not found with id: 999"));
    }

    @Test
    @WithMockUser
    void uploadOpportunityMedia_invalidFile_returns400() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "file.txt", MediaType.TEXT_PLAIN_VALUE, "bad".getBytes());
        when(mediaService.uploadOpportunityMedia(10L, file))
                .thenThrow(new InvalidFileException("Only image files are allowed"));

        mockMvc.perform(multipart("/opportunities/10/media").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Only image files are allowed"));
    }
}
