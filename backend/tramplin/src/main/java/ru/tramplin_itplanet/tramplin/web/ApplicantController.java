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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ru.tramplin_itplanet.tramplin.domain.service.ApplicantService;
import ru.tramplin_itplanet.tramplin.web.dto.ApplicantProfileResponse;
import ru.tramplin_itplanet.tramplin.web.dto.CreateApplicantRequest;
import ru.tramplin_itplanet.tramplin.web.dto.UpdateCurrentApplicantRequest;
import ru.tramplin_itplanet.tramplin.web.dto.UpdateApplicantRequest;
import ru.tramplin_itplanet.tramplin.web.dto.UpdateApplicantVisibilityRequest;
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

    @Operation(summary = "Get current applicant profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Current applicant profile found"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
            @ApiResponse(responseCode = "404", description = "Applicant profile not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public ResponseEntity<ApplicantProfileResponse> getCurrent() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authenticatedEmail(authentication);
        log.info("GET /applicants/me: email={}", email);
        return ResponseEntity.ok(ApplicantMapper.toResponse(applicantService.getCurrentByUserEmail(email)));
    }

    @Operation(summary = "Edit applicant profile by ID", description = "Updates all applicant profile fields.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Applicant profile updated"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "User role must be APPLICANT"),
            @ApiResponse(responseCode = "404", description = "Applicant or user not found"),
            @ApiResponse(responseCode = "409", description = "Applicant profile already exists for selected user")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{id}")
    public ResponseEntity<ApplicantProfileResponse> update(
            @Parameter(description = "Applicant profile id", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody UpdateApplicantRequest request) {
        log.info("POST /applicants/{} (update): userId={}", id, request.userId());
        ApplicantProfileResponse response = ApplicantMapper.toResponse(
                applicantService.update(id, ApplicantMapper.toCommand(request))
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update current applicant profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Applicant profile updated"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Current user is not an applicant"),
            @ApiResponse(responseCode = "404", description = "Applicant profile not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/me")
    public ResponseEntity<ApplicantProfileResponse> updateCurrent(@Valid @RequestBody UpdateCurrentApplicantRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authenticatedEmail(authentication);
        ensureApplicantRole(authentication);
        log.info("PUT /applicants/me: email={}", email);
        ApplicantProfileResponse response = ApplicantMapper.toResponse(
                applicantService.updateCurrentByUserEmail(email, ApplicantMapper.toCommand(request))
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update current applicant visibility")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Applicant visibility updated"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Current user is not an applicant"),
            @ApiResponse(responseCode = "404", description = "Applicant profile not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/me/visibility")
    public ResponseEntity<ApplicantProfileResponse> updateVisibility(
            @Valid @RequestBody UpdateApplicantVisibilityRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authenticatedEmail(authentication);
        ensureApplicantRole(authentication);
        log.info("PUT /applicants/me/visibility: email={}, visibility={}", email, request.visibility());

        ApplicantProfileResponse response = ApplicantMapper.toResponse(
                applicantService.updateVisibilityByUserEmail(email, request.visibility())
        );
        return ResponseEntity.ok(response);
    }

    private static String authenticatedEmail(Authentication authentication) {
        if (authentication == null
                || authentication.getName() == null
                || "anonymousUser".equals(authentication.getName())) {
            throw new BadCredentialsException("Unauthorized");
        }
        return authentication.getName();
    }

    private static void ensureApplicantRole(Authentication authentication) {
        boolean isApplicantOrCurator = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> "ROLE_APPLICANT".equals(authority) || "ROLE_CURATOR".equals(authority));
        if (!isApplicantOrCurator) {
            throw new AccessDeniedException("Forbidden");
        }
    }
}
