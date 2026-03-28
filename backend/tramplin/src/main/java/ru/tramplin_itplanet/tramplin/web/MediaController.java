package ru.tramplin_itplanet.tramplin.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.tramplin_itplanet.tramplin.domain.service.MediaService;
import ru.tramplin_itplanet.tramplin.web.dto.MediaUploadResponse;

@Tag(name = "Media", description = "Upload images to S3 and bind them to entities")
@RestController
public class MediaController {

    private static final Logger log = LoggerFactory.getLogger(MediaController.class);

    private final MediaService mediaService;

    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @Operation(summary = "Upload employer logo", description = "Uploads an image to S3 and saves object path to employers.logo_url")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Employer logo uploaded"),
            @ApiResponse(responseCode = "400", description = "Invalid image file",
                    content = @Content(schema = @Schema(example = "{\"status\":400,\"error\":\"Only image files are allowed\"}"))),
            @ApiResponse(responseCode = "404", description = "Employer not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(value = "/employers/{employerId}/logo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaUploadResponse> uploadEmployerLogo(@PathVariable Long employerId,
                                                                  @RequestParam("file") MultipartFile file) {
        log.info("POST /employers/{}/logo", employerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(mediaService.uploadEmployerLogo(employerId, file));
    }

    @Operation(summary = "Upload opportunity draft media", description = "Uploads an image to S3 before opportunity creation and returns object path/url for later usage in POST /opportunities media[]")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Opportunity draft media uploaded"),
            @ApiResponse(responseCode = "400", description = "Invalid image file",
                    content = @Content(schema = @Schema(example = "{\"status\":400,\"error\":\"Only image files are allowed\"}"))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(value = "/opportunities/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaUploadResponse> uploadOpportunityDraftMedia(@RequestParam("file") MultipartFile file) {
        log.info("POST /opportunities/media");
        return ResponseEntity.status(HttpStatus.CREATED).body(mediaService.uploadOpportunityDraftMedia(file));
    }

    @Operation(summary = "Upload opportunity media", description = "Uploads an image to S3 and immediately appends object path to opportunity_media for an existing opportunity")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Opportunity media uploaded"),
            @ApiResponse(responseCode = "400", description = "Invalid image file",
                    content = @Content(schema = @Schema(example = "{\"status\":400,\"error\":\"Only image files are allowed\"}"))),
            @ApiResponse(responseCode = "404", description = "Opportunity not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(value = "/opportunities/{opportunityId}/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaUploadResponse> uploadOpportunityMedia(@PathVariable Long opportunityId,
                                                                      @RequestParam("file") MultipartFile file) {
        log.info("POST /opportunities/{}/media", opportunityId);
        return ResponseEntity.status(HttpStatus.CREATED).body(mediaService.uploadOpportunityMedia(opportunityId, file));
    }

    @Operation(summary = "Upload applicant resume", description = "Uploads a PDF resume to S3 and saves object path to applicants.resume_url")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Applicant resume uploaded"),
            @ApiResponse(responseCode = "400", description = "Invalid PDF file",
                    content = @Content(schema = @Schema(example = "{\"status\":400,\"error\":\"Only PDF files are allowed\"}"))),
            @ApiResponse(responseCode = "404", description = "Applicant not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(value = "/applicants/{applicantId}/resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaUploadResponse> uploadApplicantResume(@PathVariable Long applicantId,
                                                                     @RequestParam("file") MultipartFile file) {
        log.info("POST /applicants/{}/resume", applicantId);
        return ResponseEntity.status(HttpStatus.CREATED).body(mediaService.uploadApplicantResume(applicantId, file));
    }

    @Operation(summary = "Upload applicant avatar", description = "Uploads an image to S3 and saves object path to applicants.avatar_url")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Applicant avatar uploaded"),
            @ApiResponse(responseCode = "400", description = "Invalid image file",
                    content = @Content(schema = @Schema(example = "{\"status\":400,\"error\":\"Only image files are allowed\"}"))),
            @ApiResponse(responseCode = "404", description = "Applicant not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(value = "/applicants/{applicantId}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MediaUploadResponse> uploadApplicantAvatar(@PathVariable Long applicantId,
                                                                     @RequestParam("file") MultipartFile file) {
        log.info("POST /applicants/{}/avatar", applicantId);
        return ResponseEntity.status(HttpStatus.CREATED).body(mediaService.uploadApplicantAvatar(applicantId, file));
    }
}
