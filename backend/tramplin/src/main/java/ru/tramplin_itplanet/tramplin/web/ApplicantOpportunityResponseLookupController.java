package ru.tramplin_itplanet.tramplin.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.tramplin_itplanet.tramplin.domain.service.OpportunityResponseService;
import ru.tramplin_itplanet.tramplin.web.dto.MyOpportunityResponseItem;
import ru.tramplin_itplanet.tramplin.web.mapper.MyOpportunityResponseItemMapper;

import java.util.List;

@Tag(name = "Opportunity Responses", description = "Applicant responses to opportunity cards")
@RestController
@RequestMapping("/applicants")
public class ApplicantOpportunityResponseLookupController {

    private static final Logger log = LoggerFactory.getLogger(ApplicantOpportunityResponseLookupController.class);

    private final OpportunityResponseService opportunityResponseService;

    public ApplicantOpportunityResponseLookupController(OpportunityResponseService opportunityResponseService) {
        this.opportunityResponseService = opportunityResponseService;
    }

    @Operation(
            summary = "Get applicant responses by applicant id",
            description = "For PRIVATE visibility only owner applicant, EMPLOYER and CURATOR can view responses."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Responses returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Responses are private for this applicant"),
            @ApiResponse(responseCode = "404", description = "Applicant profile not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{applicantId}/responses")
    public ResponseEntity<List<MyOpportunityResponseItem>> getResponsesByApplicantId(
            @Parameter(description = "Applicant profile id", example = "7")
            @PathVariable Long applicantId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authenticatedEmail(authentication);

        log.info("GET /applicants/{}/responses: email={}", applicantId, email);
        List<MyOpportunityResponseItem> response = opportunityResponseService
                .getResponsesByApplicantIdForViewer(email, applicantId).stream()
                .map(MyOpportunityResponseItemMapper::toResponse)
                .toList();
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
}
