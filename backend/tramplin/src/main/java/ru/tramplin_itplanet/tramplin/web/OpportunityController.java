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
import ru.tramplin_itplanet.tramplin.domain.service.EmployerService;
import ru.tramplin_itplanet.tramplin.domain.service.OpportunityService;
import ru.tramplin_itplanet.tramplin.web.dto.CreateOpportunityRequest;
import ru.tramplin_itplanet.tramplin.web.dto.EmployerOpportunityPostingResponse;
import ru.tramplin_itplanet.tramplin.web.dto.OpportunityCardResponse;
import ru.tramplin_itplanet.tramplin.web.dto.OpportunityMiniCardResponse;
import ru.tramplin_itplanet.tramplin.web.dto.UpdateOpportunityRequest;
import ru.tramplin_itplanet.tramplin.web.mapper.CreateOpportunityRequestMapper;
import ru.tramplin_itplanet.tramplin.web.mapper.EmployerOpportunityPostingMapper;
import ru.tramplin_itplanet.tramplin.web.mapper.OpportunityCardMapper;
import ru.tramplin_itplanet.tramplin.web.mapper.OpportunityMiniCardMapper;
import ru.tramplin_itplanet.tramplin.web.mapper.UpdateOpportunityRequestMapper;

import java.util.List;

@Tag(name = "Opportunities", description = "Career opportunities: vacancies, internships, mentorships, and events")
@RestController
@RequestMapping("/opportunities")
public class OpportunityController {

    private static final Logger log = LoggerFactory.getLogger(OpportunityController.class);

    private final OpportunityService opportunityService;
    private final EmployerService employerService;

    public OpportunityController(OpportunityService opportunityService, EmployerService employerService) {
        this.opportunityService = opportunityService;
        this.employerService = employerService;
    }

    @Operation(
            summary = "Get mini-cards for home page",
            description = "Returns opportunity mini-cards with first media, title, description, employer name, format, and first three tags. Optional full-text search is supported."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mini-cards returned successfully")
    })
    @GetMapping("/mini-cards")
    public ResponseEntity<List<OpportunityMiniCardResponse>> getMiniCards(
            @Parameter(description = "Optional full-text search query (Russian/English). Searches title, description, company name, and tags.",
                    example = "java стажировка remote")
            @RequestParam(required = false) String search) {
        log.info("GET /opportunities/mini-cards: search={}", search);
        List<OpportunityMiniCardResponse> response = opportunityService.findActiveMiniCards(search).stream()
                .map(OpportunityMiniCardMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get opportunity card by ID",
            description = "Returns the full opportunity card including employer details, assigned tags, salary range, location, and media."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Opportunity card found"),
            @ApiResponse(responseCode = "404", description = "Opportunity not found",
                    content = @Content(schema = @Schema(example = "{\"status\":404,\"error\":\"Opportunity not found with id: 1\"}")))
    })
    @GetMapping("/{id}")
    public ResponseEntity<OpportunityCardResponse> getCard(
            @Parameter(description = "ID of the opportunity", example = "1")
            @PathVariable Long id) {
        log.info("GET /opportunities/{}", id);
        return ResponseEntity.ok(OpportunityCardMapper.toResponse(opportunityService.getById(id)));
    }

    @Operation(
            summary = "Get current employer opportunities",
            description = "Returns opportunities created by the authenticated employer with applications count."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employer opportunities returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Current user is not an employer"),
            @ApiResponse(responseCode = "404", description = "Employer profile not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public ResponseEntity<List<EmployerOpportunityPostingResponse>> getMyOpportunities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authenticatedEmail();
        ensureEmployerRole(authentication);

        Long employerId = employerService.getCurrentByUserEmail(email).id();
        log.info("GET /opportunities/me: email={}, employerId={}", email, employerId);

        List<EmployerOpportunityPostingResponse> response = opportunityService.findByEmployerId(employerId).stream()
                .map(EmployerOpportunityPostingMapper::toResponse)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Create a new opportunity card",
            description = "Creates a vacancy, internship, mentorship, or career event. Returns the full created card with its generated ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Opportunity created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed — required fields missing or invalid",
                    content = @Content(schema = @Schema(example = "{\"status\":400,\"errors\":[\"title: must not be blank\"]}"))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(schema = @Schema(example = "{\"status\":401,\"error\":\"Unauthorized\"}"))),
            @ApiResponse(responseCode = "403", description = "Only full_verified employer owner can create opportunity",
                    content = @Content(schema = @Schema(example = "{\"status\":403,\"error\":\"Employer must have full_verified status\"}"))),
            @ApiResponse(responseCode = "404", description = "Referenced employer not found",
                    content = @Content(schema = @Schema(example = "{\"status\":404,\"error\":\"Employer not found with id: 5\"}")))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<OpportunityCardResponse> create(@Valid @RequestBody CreateOpportunityRequest request) {
        String email = authenticatedEmail();
        employerService.assertCanManageOpportunities(email, request.employerId());
        log.info("POST /opportunities: title={}, type={}, employerId={}", request.title(), request.type(), request.employerId());
        OpportunityCardResponse response = OpportunityCardMapper.toResponse(
                opportunityService.create(CreateOpportunityRequestMapper.toCommand(request))
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(
            summary = "Update opportunity card by ID",
            description = "Updates all editable opportunity fields. The card ID is immutable."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Opportunity updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed — required fields missing or invalid",
                    content = @Content(schema = @Schema(example = "{\"status\":400,\"errors\":[\"title: must not be blank\"]}"))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(schema = @Schema(example = "{\"status\":401,\"error\":\"Unauthorized\"}"))),
            @ApiResponse(responseCode = "403", description = "Only full_verified employer owner can update opportunity",
                    content = @Content(schema = @Schema(example = "{\"status\":403,\"error\":\"Employer must have full_verified status\"}"))),
            @ApiResponse(responseCode = "404", description = "Opportunity or referenced employer not found",
                    content = @Content(schema = @Schema(example = "{\"status\":404,\"error\":\"Opportunity not found with id: 5\"}")))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    public ResponseEntity<OpportunityCardResponse> update(
            @Parameter(description = "ID of the opportunity", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody UpdateOpportunityRequest request) {
        String email = authenticatedEmail();
        employerService.assertCanManageOpportunities(email, request.employerId());
        log.info("PUT /opportunities/{}: title={}, type={}, employerId={}", id, request.title(), request.type(), request.employerId());
        OpportunityCardResponse response = OpportunityCardMapper.toResponse(
                opportunityService.update(id, UpdateOpportunityRequestMapper.toCommand(request))
        );
        return ResponseEntity.ok(response);
    }

    private static String authenticatedEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || authentication.getName() == null
                || "anonymousUser".equals(authentication.getName())) {
            throw new BadCredentialsException("Unauthorized");
        }
        return authentication.getName();
    }

    private static void ensureEmployerRole(Authentication authentication) {
        boolean isEmployer = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_EMPLOYER"::equals);
        if (!isEmployer) {
            throw new AccessDeniedException("Forbidden");
        }
    }
}
