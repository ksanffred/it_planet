package ru.tramplin_itplanet.tramplin.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.*;
import ru.tramplin_itplanet.tramplin.domain.service.EmployerService;
import ru.tramplin_itplanet.tramplin.web.dto.EmployerProfileResponse;
import ru.tramplin_itplanet.tramplin.web.dto.RegisterEmployerRequest;
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
}
