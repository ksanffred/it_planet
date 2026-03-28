package ru.tramplin_itplanet.tramplin.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.tramplin_itplanet.tramplin.domain.service.ApplicantService;
import ru.tramplin_itplanet.tramplin.web.dto.ApplicantProfileResponse;
import ru.tramplin_itplanet.tramplin.web.dto.CreateApplicantRequest;
import ru.tramplin_itplanet.tramplin.web.mapper.ApplicantMapper;

@Tag(name = "Applicants", description = "Applicant personal profiles")
@RestController
@RequestMapping("/applicants")
public class ApplicantController {

    private static final Logger log = LoggerFactory.getLogger(ApplicantController.class);

    private final ApplicantService applicantService;

    public ApplicantController(ApplicantService applicantService) {
        this.applicantService = applicantService;
    }

    @Operation(summary = "Create applicant profile")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Applicant profile created"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "User role must be APPLICANT"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "409", description = "Applicant profile already exists")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<ApplicantProfileResponse> create(@Valid @RequestBody CreateApplicantRequest request) {
        log.info("POST /applicants: userId={}", request.userId());
        ApplicantProfileResponse response = ApplicantMapper.toResponse(
                applicantService.create(ApplicantMapper.toCommand(request))
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get applicant profile by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Applicant profile found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "404", description = "Applicant profile not found",
                    content = @Content(schema = @Schema(example = "{\"status\":404,\"error\":\"Applicant not found with id: 1\"}")))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{id}")
    public ResponseEntity<ApplicantProfileResponse> getById(
            @Parameter(description = "Applicant profile id", example = "1")
            @PathVariable Long id) {
        log.info("GET /applicants/{}", id);
        return ResponseEntity.ok(ApplicantMapper.toResponse(applicantService.getById(id)));
    }
}
