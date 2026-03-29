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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.*;
import ru.tramplin_itplanet.tramplin.domain.service.EmployerService;
import ru.tramplin_itplanet.tramplin.web.dto.EmployerProfileResponse;
import ru.tramplin_itplanet.tramplin.web.dto.RegisterEmployerRequest;
import ru.tramplin_itplanet.tramplin.web.dto.UpdateEmployerByCuratorRequest;
import ru.tramplin_itplanet.tramplin.web.dto.UpdateEmployerRequest;
import ru.tramplin_itplanet.tramplin.web.mapper.EmployerMapper;

@Tag(name = "Employers", description = "Employers and organizers")
@RestController
@RequestMapping("/employers")
public class EmployerController {

    private static final Logger log = LoggerFactory.getLogger(EmployerController.class);

    private final EmployerService employerService;

    public EmployerController(EmployerService employerService) {
        this.employerService = employerService;
    }

    @Operation(summary = "Register employer", description = "Creates a new employer. Only INN is required.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Employer created"),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(example = "{\"status\":400,\"errors\":[\"inn: must not be blank\"]}")))
    })
    @PostMapping("/register")
    public ResponseEntity<EmployerProfileResponse> register(@Valid @RequestBody RegisterEmployerRequest request) {
        log.info("POST /employers/register: inn={}", request.inn());
        EmployerProfileResponse response = EmployerMapper.toResponse(
                employerService.register(EmployerMapper.toCommand(request))
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get employer by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employer found"),
            @ApiResponse(responseCode = "404", description = "Employer not found",
                    content = @Content(schema = @Schema(example = "{\"status\":404,\"error\":\"Employer not found with id: 1\"}")))
    })
    @GetMapping("/{id}")
    public ResponseEntity<EmployerProfileResponse> getById(
            @Parameter(description = "Employer id", example = "1")
            @PathVariable Long id) {
        log.info("GET /employers/{}", id);
        return ResponseEntity.ok(EmployerMapper.toResponse(employerService.getById(id)));
    }

    @Operation(summary = "Get current employer profile")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Current employer profile returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(schema = @Schema(example = "{\"status\":401,\"error\":\"Unauthorized\"}"))),
            @ApiResponse(responseCode = "404", description = "Employer profile not found",
                    content = @Content(schema = @Schema(example = "{\"status\":404,\"error\":\"Employer not found with id: 1\"}")))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public ResponseEntity<EmployerProfileResponse> getCurrent() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authenticatedEmail(authentication);
        log.info("GET /employers/me: email={}", email);
        return ResponseEntity.ok(EmployerMapper.toResponse(employerService.getCurrentByUserEmail(email)));
    }

    @Operation(summary = "Update current employer profile", description = "Updates editable employer fields. companyName and inn are immutable after creation.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employer profile updated"),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(example = "{\"status\":400,\"errors\":[\"companyName: must not be blank\"]}"))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(schema = @Schema(example = "{\"status\":401,\"error\":\"Unauthorized\"}"))),
            @ApiResponse(responseCode = "403", description = "Current user is not an employer",
                    content = @Content(schema = @Schema(example = "{\"status\":403,\"error\":\"Forbidden\"}"))),
            @ApiResponse(responseCode = "404", description = "Employer profile not found",
                    content = @Content(schema = @Schema(example = "{\"status\":404,\"error\":\"Employer not found with id: 1\"}")))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/me")
    public ResponseEntity<EmployerProfileResponse> updateCurrent(@Valid @RequestBody UpdateEmployerRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authenticatedEmail(authentication);
        ensureEmployerRole(authentication);
        log.info("PUT /employers/me: email={}", email);
        EmployerProfileResponse response = EmployerMapper.toResponse(
                employerService.updateCurrentByUserEmail(email, EmployerMapper.toCommand(request))
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update employer profile by id (curator only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employer profile updated"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Current user is not a curator"),
            @ApiResponse(responseCode = "404", description = "Employer profile not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    public ResponseEntity<EmployerProfileResponse> updateById(
            @Parameter(description = "Employer id", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody UpdateEmployerByCuratorRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authenticatedEmail(authentication);
        ensureCuratorRole(authentication);
        log.info("PUT /employers/{}: email={}", id, email);
        EmployerProfileResponse response = EmployerMapper.toResponse(
                employerService.updateByIdAsCurator(email, id, EmployerMapper.toCommand(request))
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete employer profile by id (curator only)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Employer profile deleted"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token"),
            @ApiResponse(responseCode = "403", description = "Current user is not a curator"),
            @ApiResponse(responseCode = "404", description = "Employer profile not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(
            @Parameter(description = "Employer id", example = "1")
            @PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authenticatedEmail(authentication);
        ensureCuratorRole(authentication);
        log.info("DELETE /employers/{}: email={}", id, email);
        employerService.deleteByIdAsCurator(email, id);
        return ResponseEntity.noContent().build();
    }

    private static String authenticatedEmail(Authentication authentication) {
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

    private static void ensureCuratorRole(Authentication authentication) {
        boolean isCurator = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_CURATOR"::equals);
        if (!isCurator) {
            throw new AccessDeniedException("Forbidden");
        }
    }
}
