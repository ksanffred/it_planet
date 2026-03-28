package ru.tramplin_itplanet.tramplin.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.tramplin_itplanet.tramplin.domain.service.ApplicantContactService;
import ru.tramplin_itplanet.tramplin.web.dto.ApplicantContactPreviewResponse;
import ru.tramplin_itplanet.tramplin.web.dto.ApplicantContactResponse;
import ru.tramplin_itplanet.tramplin.web.dto.UpdateApplicantContactStatusRequest;
import ru.tramplin_itplanet.tramplin.web.mapper.ApplicantContactPreviewMapper;
import ru.tramplin_itplanet.tramplin.web.mapper.ApplicantContactMapper;

import java.util.List;

@Tag(name = "Applicant Contacts", description = "Professional contacts between applicants")
@RestController
@RequestMapping("/applicants/me/contacts")
public class ApplicantContactController {

    private static final Logger log = LoggerFactory.getLogger(ApplicantContactController.class);

    private final ApplicantContactService applicantContactService;

    public ApplicantContactController(ApplicantContactService applicantContactService) {
        this.applicantContactService = applicantContactService;
    }

    @Operation(summary = "Create contact request")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Contact request created"),
            @ApiResponse(responseCode = "400", description = "Invalid operation"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Current user is not an applicant"),
            @ApiResponse(responseCode = "404", description = "Applicant profile not found"),
            @ApiResponse(responseCode = "409", description = "Contact request already exists")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{recipientApplicantId}")
    public ResponseEntity<ApplicantContactResponse> create(
            @Parameter(description = "Recipient applicant id", example = "7")
            @PathVariable Long recipientApplicantId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authenticatedEmail(authentication);
        ensureApplicantRole(authentication);

        log.info("POST /applicants/me/contacts/{}: email={}", recipientApplicantId, email);
        ApplicantContactResponse response = ApplicantContactMapper.toResponse(
                applicantContactService.create(email, recipientApplicantId)
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get my contacts")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contacts returned"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Current user is not an applicant")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<List<ApplicantContactPreviewResponse>> getMyContacts() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authenticatedEmail(authentication);
        ensureApplicantRole(authentication);

        log.info("GET /applicants/me/contacts: email={}", email);
        List<ApplicantContactPreviewResponse> response = applicantContactService.getMyContacts(email).stream()
                .map(ApplicantContactPreviewMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Accept or reject contact request")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contact status updated"),
            @ApiResponse(responseCode = "400", description = "Invalid operation"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Current user is not invited recipient"),
            @ApiResponse(responseCode = "404", description = "Contact request not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{contactId}/status")
    public ResponseEntity<ApplicantContactResponse> updateStatus(
            @Parameter(description = "Contact request id", example = "12")
            @PathVariable Long contactId,
            @Valid @RequestBody UpdateApplicantContactStatusRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authenticatedEmail(authentication);
        ensureApplicantRole(authentication);

        log.info("PUT /applicants/me/contacts/{}/status: email={}, status={}", contactId, email, request.status());
        ApplicantContactResponse response = ApplicantContactMapper.toResponse(
                applicantContactService.updateStatus(email, contactId, request.status())
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
        boolean isApplicant = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_APPLICANT"::equals);
        if (!isApplicant) {
            throw new AccessDeniedException("Forbidden");
        }
    }
}
