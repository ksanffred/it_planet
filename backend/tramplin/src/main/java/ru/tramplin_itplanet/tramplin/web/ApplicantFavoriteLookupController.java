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
import ru.tramplin_itplanet.tramplin.domain.service.ApplicantFavoriteService;
import ru.tramplin_itplanet.tramplin.web.dto.ApplicantFavoriteOpportunityCardResponse;
import ru.tramplin_itplanet.tramplin.web.mapper.ApplicantFavoriteOpportunityCardMapper;

import java.util.List;

@Tag(name = "Applicant Favorites", description = "Favorites visibility lookups for applicant users")
@RestController
@RequestMapping("/applicants")
public class ApplicantFavoriteLookupController {

    private static final Logger log = LoggerFactory.getLogger(ApplicantFavoriteLookupController.class);

    private final ApplicantFavoriteService applicantFavoriteService;

    public ApplicantFavoriteLookupController(ApplicantFavoriteService applicantFavoriteService) {
        this.applicantFavoriteService = applicantFavoriteService;
    }

    @Operation(
            summary = "Get applicant favorite opportunity cards by applicant id",
            description = "For PRIVATE visibility only owner applicant, EMPLOYER and ADMIN can view cards."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Favorite cards returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Favorites are private for this applicant"),
            @ApiResponse(responseCode = "404", description = "Applicant profile not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/{applicantId}/favorites/opportunities")
    public ResponseEntity<List<ApplicantFavoriteOpportunityCardResponse>> getCardsByApplicantId(
            @Parameter(description = "Applicant profile id", example = "7")
            @PathVariable Long applicantId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authenticatedEmail(authentication);

        log.info("GET /applicants/{}/favorites/opportunities: email={}", applicantId, email);

        List<ApplicantFavoriteOpportunityCardResponse> response = applicantFavoriteService
                .getCardsByApplicantIdForViewer(email, applicantId).stream()
                .map(ApplicantFavoriteOpportunityCardMapper::toResponse)
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
