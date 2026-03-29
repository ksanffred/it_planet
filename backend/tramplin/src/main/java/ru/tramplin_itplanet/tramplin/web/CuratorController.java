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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.tramplin_itplanet.tramplin.domain.service.CuratorService;
import ru.tramplin_itplanet.tramplin.web.dto.CreateCuratorRequest;
import ru.tramplin_itplanet.tramplin.web.dto.CuratorResponse;
import ru.tramplin_itplanet.tramplin.web.mapper.CuratorMapper;

@Tag(name = "Curators", description = "Curator management")
@RestController
@RequestMapping("/curators")
public class CuratorController {

    private static final Logger log = LoggerFactory.getLogger(CuratorController.class);

    private final CuratorService curatorService;

    public CuratorController(CuratorService curatorService) {
        this.curatorService = curatorService;
    }

    @Operation(summary = "Create curator", description = "Only curator with id 1 can create curators")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Curator created"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Only curator with id 1 can create curators"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "409", description = "Curator already exists")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<CuratorResponse> create(@Valid @RequestBody CreateCuratorRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        ensureCuratorRole(authentication);
        String email = authenticatedEmail();
        log.info("POST /curators: email={}, userId={}", email, request.userId());
        CuratorResponse response = CuratorMapper.toResponse(curatorService.create(email, request.userId()));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Delete curator", description = "Only curator with id 1 can delete curators")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Curator deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Only curator with id 1 can delete curators"),
            @ApiResponse(responseCode = "404", description = "Curator not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{curatorId}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "Curator id", example = "2")
            @PathVariable Long curatorId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        ensureCuratorRole(authentication);
        String email = authenticatedEmail();
        log.info("DELETE /curators/{}: email={}", curatorId, email);
        curatorService.delete(email, curatorId);
        return ResponseEntity.noContent().build();
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

    private static void ensureCuratorRole(Authentication authentication) {
        boolean isCurator = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_CURATOR"::equals);
        if (!isCurator) {
            throw new AccessDeniedException("Forbidden");
        }
    }
}
