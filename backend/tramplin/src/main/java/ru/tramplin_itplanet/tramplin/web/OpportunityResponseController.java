package ru.tramplin_itplanet.tramplin.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.tramplin_itplanet.tramplin.domain.service.OpportunityResponseService;
import ru.tramplin_itplanet.tramplin.web.dto.MyOpportunityResponseItem;
import ru.tramplin_itplanet.tramplin.web.dto.OpportunityResponseCreatedResponse;
import ru.tramplin_itplanet.tramplin.web.mapper.MyOpportunityResponseItemMapper;
import ru.tramplin_itplanet.tramplin.web.mapper.OpportunityResponseMapper;

import java.util.List;

@Tag(name = "Opportunity Responses", description = "Applicant responses to opportunity cards")
@RestController
@RequestMapping("/opportunities")
public class OpportunityResponseController {

    private static final Logger log = LoggerFactory.getLogger(OpportunityResponseController.class);

    private final OpportunityResponseService opportunityResponseService;

    public OpportunityResponseController(OpportunityResponseService opportunityResponseService) {
        this.opportunityResponseService = opportunityResponseService;
    }

    @Operation(
            summary = "Apply to an opportunity",
            description = "Creates applicant response to an opportunity with default status NOT_REVIEWED."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Response created"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Current user is not an applicant"),
            @ApiResponse(responseCode = "404", description = "Opportunity or applicant not found"),
            @ApiResponse(responseCode = "409", description = "Applicant already responded to this opportunity",
                    content = @Content(schema = @Schema(example = "{\"status\":409,\"error\":\"Response already exists for opportunity id: 10 and applicant id: 3\"}")))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{opportunityId}/responses")
    public ResponseEntity<OpportunityResponseCreatedResponse> apply(
            @Parameter(description = "Opportunity id", example = "10")
            @PathVariable Long opportunityId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authenticatedEmail(authentication);
        ensureApplicantRole(authentication);

        log.info("POST /opportunities/{}/responses: email={}", opportunityId, email);
        OpportunityResponseCreatedResponse response = OpportunityResponseMapper.toResponse(
                opportunityResponseService.apply(opportunityId, email)
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Get opportunities I responded to",
            description = "Returns title, company_name, response status, opportunity type, and opportunity status."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Current user is not an applicant"),
            @ApiResponse(responseCode = "404", description = "Applicant profile not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/responses/me")
    public ResponseEntity<List<MyOpportunityResponseItem>> myResponses() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authenticatedEmail(authentication);
        ensureApplicantRole(authentication);

        log.info("GET /opportunities/responses/me: email={}", email);
        List<MyOpportunityResponseItem> response = opportunityResponseService.getMyResponses(email).stream()
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

    private static void ensureApplicantRole(Authentication authentication) {
        boolean isApplicant = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_APPLICANT"::equals);
        if (!isApplicant) {
            throw new AccessDeniedException("Forbidden");
        }
    }
}
