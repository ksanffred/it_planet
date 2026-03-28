package ru.tramplin_itplanet.tramplin.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import ru.tramplin_itplanet.tramplin.domain.model.UserRole;
import ru.tramplin_itplanet.tramplin.domain.service.AuthService;
import ru.tramplin_itplanet.tramplin.web.dto.AuthResponse;
import ru.tramplin_itplanet.tramplin.web.dto.CurrentUserResponse;
import ru.tramplin_itplanet.tramplin.web.dto.LoginRequest;
import ru.tramplin_itplanet.tramplin.web.dto.RegisterRequest;

@Tag(name = "Authentication", description = "Register and login to obtain a JWT token")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Register a new user", description = "Creates a new account and returns a JWT token.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(example = "{\"status\":400,\"errors\":[\"email: must be a well-formed email address\"]}"))),
            @ApiResponse(responseCode = "409", description = "Email already in use",
                    content = @Content(schema = @Schema(example = "{\"status\":409,\"error\":\"User already exists with email: user@example.com\"}")))
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserRole role = request.role() != null ? request.role() : UserRole.APPLICANT;
        log.info("POST /auth/register: email={}, role={}", request.email(), role);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authService.register(request.email(), request.displayName(), request.password(), role));
    }

    @Operation(summary = "Login", description = "Authenticates with email and password, returns a JWT token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content = @Content(schema = @Schema(example = "{\"status\":401,\"error\":\"Invalid email or password\"}")))
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("POST /auth/login: email={}", request.email());
        return ResponseEntity.ok(authService.login(request.email(), request.password()));
    }

    @Operation(summary = "Get current user", description = "Returns data for the currently authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Current user returned"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT token",
                    content = @Content(schema = @Schema(example = "{\"status\":401,\"error\":\"Unauthorized\"}")))
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public ResponseEntity<CurrentUserResponse> me() {
        log.info("GET /auth/me");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || "anonymousUser".equals(authentication.getName())) {
            throw new BadCredentialsException("Unauthorized");
        }
        return ResponseEntity.ok(authService.getCurrentUser(authentication.getName()));
    }

    @Operation(summary = "Verify email address")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Email verified"),
            @ApiResponse(responseCode = "400", description = "Token invalid or expired")
    })
    @GetMapping("/verify")
    public ResponseEntity<Map<String, String>> verify(@RequestParam String token) {
        log.info("GET /auth/verify");
        authService.verifyEmail(token);
        return ResponseEntity.ok(Map.of("message", "Email verified successfully"));
    }
}
