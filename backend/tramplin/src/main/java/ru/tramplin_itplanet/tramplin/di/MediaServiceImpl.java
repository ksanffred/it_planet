package ru.tramplin_itplanet.tramplin.di;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.tramplin_itplanet.tramplin.datasource.entity.EmployerEntity;
import ru.tramplin_itplanet.tramplin.datasource.entity.OpportunityEntity;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaEmployerRepository;
import ru.tramplin_itplanet.tramplin.datasource.jpa.JpaOpportunityRepository;
import ru.tramplin_itplanet.tramplin.domain.exception.EmployerNotFoundException;
import ru.tramplin_itplanet.tramplin.domain.exception.FileStorageException;
import ru.tramplin_itplanet.tramplin.domain.exception.InvalidFileException;
import ru.tramplin_itplanet.tramplin.domain.exception.OpportunityNotFoundException;
import ru.tramplin_itplanet.tramplin.domain.service.MediaService;
import ru.tramplin_itplanet.tramplin.web.dto.MediaUploadResponse;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Service
public class MediaServiceImpl implements MediaService {

    private static final Logger log = LoggerFactory.getLogger(MediaServiceImpl.class);

    private static final Set<String> SUPPORTED_IMAGE_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif",
            "image/svg+xml"
    );

    private final S3Client s3Client;
    private final JpaEmployerRepository jpaEmployerRepository;
    private final JpaOpportunityRepository jpaOpportunityRepository;

    @Value("${app.s3.bucket}")
    private String bucket;

    @Value("${app.s3.public-base-url:}")
    private String publicBaseUrl;

    public MediaServiceImpl(S3Client s3Client,
                            JpaEmployerRepository jpaEmployerRepository,
                            JpaOpportunityRepository jpaOpportunityRepository) {
        this.s3Client = s3Client;
        this.jpaEmployerRepository = jpaEmployerRepository;
        this.jpaOpportunityRepository = jpaOpportunityRepository;
    }

    @Override
    @Transactional
    public MediaUploadResponse uploadEmployerLogo(Long employerId, MultipartFile file) {
        validateImageFile(file);
        EmployerEntity employer = jpaEmployerRepository.findById(employerId)
                .orElseThrow(() -> new EmployerNotFoundException(employerId));

        String objectPath = buildObjectPath("employers/" + employerId, file.getOriginalFilename());
        uploadToS3(objectPath, file);

        employer.setLogoUrl(objectPath);
        jpaEmployerRepository.save(employer);
        log.info("Employer logo uploaded: employerId={}, path={}", employerId, objectPath);
        return new MediaUploadResponse(objectPath, toPublicUrl(objectPath));
    }

    @Override
    @Transactional
    public MediaUploadResponse uploadOpportunityMedia(Long opportunityId, MultipartFile file) {
        validateImageFile(file);
        OpportunityEntity opportunity = jpaOpportunityRepository.findById(opportunityId)
                .orElseThrow(() -> new OpportunityNotFoundException(opportunityId));

        String objectPath = buildObjectPath("opportunities/" + opportunityId, file.getOriginalFilename());
        uploadToS3(objectPath, file);

        opportunity.getMedia().add(objectPath);
        jpaOpportunityRepository.save(opportunity);
        log.info("Opportunity media uploaded: opportunityId={}, path={}", opportunityId, objectPath);
        return new MediaUploadResponse(objectPath, toPublicUrl(objectPath));
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("Uploaded file is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !SUPPORTED_IMAGE_CONTENT_TYPES.contains(contentType)) {
            throw new InvalidFileException("Only image files are allowed");
        }
    }

    private void uploadToS3(String objectPath, MultipartFile file) {
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectPath)
                    .contentType(file.getContentType())
                    .build();
            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } catch (IOException | S3Exception ex) {
            log.error("Failed to upload file to S3: path={}, message={}", objectPath, ex.getMessage());
            throw new FileStorageException("Failed to upload file to S3", ex);
        }
    }

    private String buildObjectPath(String prefix, String originalFilename) {
        String extension = extractExtension(originalFilename);
        return prefix + "/" + UUID.randomUUID() + extension;
    }

    private String extractExtension(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "";
        }
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == originalFilename.length() - 1) {
            return "";
        }
        return originalFilename.substring(dotIndex).toLowerCase();
    }

    private String toPublicUrl(String objectPath) {
        if (publicBaseUrl == null || publicBaseUrl.isBlank()) {
            return null;
        }
        return publicBaseUrl.endsWith("/")
                ? publicBaseUrl + objectPath
                : publicBaseUrl + "/" + objectPath;
    }
}
