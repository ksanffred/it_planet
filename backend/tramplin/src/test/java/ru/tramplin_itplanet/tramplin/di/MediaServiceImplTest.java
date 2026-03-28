package ru.tramplin_itplanet.tramplin.di;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import ru.tramplin_itplanet.tramplin.datasource.entity.EmployerEntity;
import ru.tramplin_itplanet.tramplin.datasource.entity.OpportunityEntity;
import ru.tramplin_itplanet.tramplin.datasource.entity.ApplicantEntity;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaApplicantRepository;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaEmployerRepository;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaOpportunityRepository;
import ru.tramplin_itplanet.tramplin.domain.exception.InvalidFileException;
import ru.tramplin_itplanet.tramplin.web.dto.MediaUploadResponse;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MediaServiceImplTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private JpaEmployerRepository jpaEmployerRepository;

    @Mock
    private JpaOpportunityRepository jpaOpportunityRepository;

    @Mock
    private JpaApplicantRepository jpaApplicantRepository;

    private MediaServiceImpl mediaService;

    @BeforeEach
    void setUp() {
        mediaService = new MediaServiceImpl(s3Client, jpaEmployerRepository, jpaOpportunityRepository, jpaApplicantRepository);
        ReflectionTestUtils.setField(mediaService, "bucket", "test-bucket");
        ReflectionTestUtils.setField(mediaService, "publicBaseUrl", "https://cdn.test");
    }

    @Test
    void uploadEmployerLogo_validImage_uploadsToS3AndSavesPath() {
        EmployerEntity employer = new EmployerEntity();
        employer.setId(5L);

        MockMultipartFile file = new MockMultipartFile("file", "logo.png", "image/png", "img".getBytes());

        when(jpaEmployerRepository.findById(5L)).thenReturn(Optional.of(employer));
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().eTag("etag").build());

        MediaUploadResponse response = mediaService.uploadEmployerLogo(5L, file);

        assertNotNull(response.path());
        assertTrue(response.path().startsWith("employers/5/"));
        assertTrue(response.path().endsWith(".png"));
        assertEquals(response.path(), employer.getLogoUrl());
        assertEquals("https://cdn.test/" + response.path(), response.url());
        verify(jpaEmployerRepository).save(employer);
    }

    @Test
    void uploadOpportunityMedia_validImage_uploadsToS3AndAppendsPath() {
        OpportunityEntity opportunity = new OpportunityEntity();
        opportunity.setId(7L);

        MockMultipartFile file = new MockMultipartFile("file", "photo.webp", "image/webp", "img".getBytes());

        when(jpaOpportunityRepository.findById(7L)).thenReturn(Optional.of(opportunity));
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().eTag("etag").build());

        MediaUploadResponse response = mediaService.uploadOpportunityMedia(7L, file);

        assertNotNull(response.path());
        assertTrue(response.path().startsWith("opportunities/7/"));
        assertTrue(response.path().endsWith(".webp"));
        assertEquals(1, opportunity.getMedia().size());
        assertEquals(response.path(), opportunity.getMedia().getFirst());
        verify(jpaOpportunityRepository).save(opportunity);
    }

    @Test
    void uploadOpportunityDraftMedia_validImage_uploadsToS3WithoutPersistingInDb() {
        MockMultipartFile file = new MockMultipartFile("file", "photo.webp", "image/webp", "img".getBytes());

        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().eTag("etag").build());

        MediaUploadResponse response = mediaService.uploadOpportunityDraftMedia(file);

        assertNotNull(response.path());
        assertTrue(response.path().startsWith("opportunities/drafts/"));
        assertTrue(response.path().endsWith(".webp"));
        assertEquals("https://cdn.test/" + response.path(), response.url());
        verify(jpaOpportunityRepository, never()).save(any());
    }

    @Test
    void uploadEmployerLogo_invalidMimeType_throwsBadRequestError() {
        MockMultipartFile file = new MockMultipartFile("file", "note.txt", "text/plain", "bad".getBytes());

        InvalidFileException exception = assertThrows(InvalidFileException.class,
                () -> mediaService.uploadEmployerLogo(1L, file));

        assertEquals("Only image files are allowed", exception.getMessage());
    }

    @Test
    void uploadApplicantResume_validPdf_uploadsToS3AndSavesPath() {
        ApplicantEntity applicant = new ApplicantEntity();
        applicant.setId(9L);

        MockMultipartFile file = new MockMultipartFile("file", "resume.pdf", "application/pdf", "pdf".getBytes());

        when(jpaApplicantRepository.findById(9L)).thenReturn(Optional.of(applicant));
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().eTag("etag").build());

        MediaUploadResponse response = mediaService.uploadApplicantResume(9L, file);

        assertNotNull(response.path());
        assertTrue(response.path().startsWith("applicants/9/resume/"));
        assertTrue(response.path().endsWith(".pdf"));
        assertEquals(response.path(), applicant.getResumeUrl());
        verify(jpaApplicantRepository).save(applicant);
    }

    @Test
    void uploadApplicantAvatar_validImage_uploadsToS3AndSavesPath() {
        ApplicantEntity applicant = new ApplicantEntity();
        applicant.setId(9L);

        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", "img".getBytes());

        when(jpaApplicantRepository.findById(9L)).thenReturn(Optional.of(applicant));
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(PutObjectResponse.builder().eTag("etag").build());

        MediaUploadResponse response = mediaService.uploadApplicantAvatar(9L, file);

        assertNotNull(response.path());
        assertTrue(response.path().startsWith("applicants/9/avatar/"));
        assertTrue(response.path().endsWith(".png"));
        assertEquals(response.path(), applicant.getAvatarUrl());
        verify(jpaApplicantRepository).save(applicant);
    }
}
