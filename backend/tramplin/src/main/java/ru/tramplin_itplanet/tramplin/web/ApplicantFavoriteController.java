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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.tramplin_itplanet.tramplin.domain.service.ApplicantFavoriteService;
import ru.tramplin_itplanet.tramplin.web.dto.AddFavoritesRequest;
import ru.tramplin_itplanet.tramplin.web.dto.ApplicantFavoritesResponse;
import ru.tramplin_itplanet.tramplin.web.mapper.ApplicantFavoritesMapper;

@Tag(name = "Applicant Favorites", description = "Favorites for applicant users")
@RestController
@RequestMapping("/applicants/me/favorites/opportunities")
public class ApplicantFavoriteController {

    private static final Logger log = LoggerFactory.getLogger(ApplicantFavoriteController.class);

    private final ApplicantFavoriteService applicantFavoriteService;

    public ApplicantFavoriteController(ApplicantFavoriteService applicantFavoriteService) {
        this.applicantFavoriteService = applicantFavoriteService;
    }

    @Operation(
            summary = "Add one opportunity to current applicant favorites",
            description = "Available only for APPLICANT users."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Opportunity added to favorites"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Current user is not an applicant"),
            @ApiResponse(responseCode = "404", description = "Applicant profile or opportunity not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/{opportunityId}")
    public ResponseEntity<ApplicantFavoritesResponse> addOne(
            @Parameter(description = "Opportunity card id", example = "42")
            @PathVariable Long opportunityId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authenticatedEmail(authentication);
        ensureApplicantRole(authentication);

        log.info("POST /applicants/me/favorites/opportunities/{}: email={}", opportunityId, email);

        ApplicantFavoritesResponse response = ApplicantFavoritesMapper.toResponse(
                applicantFavoriteService.addOneByUserEmail(email, opportunityId)
        );
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Add many opportunities to current applicant favorites",
            description = "Use this endpoint after login to sync favorites previously stored only on frontend for anonymous users."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Opportunities added to favorites"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Current user is not an applicant"),
            @ApiResponse(responseCode = "404", description = "Applicant profile or one of opportunities not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/bulk")
    public ResponseEntity<ApplicantFavoritesResponse> addMany(@Valid @RequestBody AddFavoritesRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authenticatedEmail(authentication);
        ensureApplicantRole(authentication);

        log.info(
                "POST /applicants/me/favorites/opportunities/bulk: email={}, opportunityCount={}",
                email,
                request.opportunityIds().size()
        );

        ApplicantFavoritesResponse response = ApplicantFavoritesMapper.toResponse(
                applicantFavoriteService.addManyByUserEmail(email, request.opportunityIds())
        );
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Remove one opportunity from current applicant favorites",
            description = "Available only for APPLICANT users."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Opportunity removed from favorites"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Current user is not an applicant"),
            @ApiResponse(responseCode = "404", description = "Applicant profile not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{opportunityId}")
    public ResponseEntity<ApplicantFavoritesResponse> removeOne(
            @Parameter(description = "Opportunity card id", example = "42")
            @PathVariable Long opportunityId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authenticatedEmail(authentication);
        ensureApplicantRole(authentication);

        log.info("DELETE /applicants/me/favorites/opportunities/{}: email={}", opportunityId, email);

        ApplicantFavoritesResponse response = ApplicantFavoritesMapper.toResponse(
                applicantFavoriteService.removeOneByUserEmail(email, opportunityId)
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
