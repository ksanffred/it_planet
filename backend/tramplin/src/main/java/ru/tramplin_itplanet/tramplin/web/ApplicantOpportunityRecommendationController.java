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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.tramplin_itplanet.tramplin.domain.service.ApplicantOpportunityRecommendationService;
import ru.tramplin_itplanet.tramplin.web.dto.ApplicantOpportunityRecommendationResponse;
import ru.tramplin_itplanet.tramplin.web.mapper.ApplicantOpportunityRecommendationMapper;

@Tag(name = "Applicant Recommendations", description = "Recommendations between verified applicant contacts")
@RestController
@RequestMapping("/opportunities")
public class ApplicantOpportunityRecommendationController {

    private static final Logger log = LoggerFactory.getLogger(ApplicantOpportunityRecommendationController.class);

    private final ApplicantOpportunityRecommendationService recommendationService;

    public ApplicantOpportunityRecommendationController(
            ApplicantOpportunityRecommendationService recommendationService
    ) {
        this.recommendationService = recommendationService;
    }

    @Operation(
            summary = "Recommend an applicant on an opportunity card",
            description = "Available only for APPLICANT users. Recommended applicant must be in current applicant verified contacts."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Recommendation created"),
            @ApiResponse(responseCode = "400", description = "Invalid recommendation operation"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Current user is not an applicant"),
            @ApiResponse(responseCode = "404", description = "Applicant profile, recommended applicant or opportunity not found"),
            @ApiResponse(responseCode = "409", description = "Recommendation already exists",
                    content = @Content(schema = @Schema(
                            example = "{\"status\":409,\"error\":\"Recommendation already exists for recommender id: 3, recommended applicant id: 7, opportunity id: 10\"}"
                    )))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{opportunityId}/recommendations/{recommendedApplicantId}")
    public ResponseEntity<ApplicantOpportunityRecommendationResponse> create(
            @Parameter(description = "Opportunity card id", example = "10")
            @PathVariable Long opportunityId,
            @Parameter(description = "Recommended applicant id", example = "7")
            @PathVariable Long recommendedApplicantId
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authenticatedEmail(authentication);
        ensureApplicantRole(authentication);

        log.info(
                "POST /opportunities/{}/recommendations/{}: email={}",
                opportunityId,
                recommendedApplicantId,
                email
        );

        ApplicantOpportunityRecommendationResponse response = ApplicantOpportunityRecommendationMapper.toResponse(
                recommendationService.create(email, opportunityId, recommendedApplicantId)
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
