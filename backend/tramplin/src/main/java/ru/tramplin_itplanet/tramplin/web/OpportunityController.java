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
import ru.tramplin_itplanet.tramplin.domain.service.OpportunityService;
import ru.tramplin_itplanet.tramplin.web.dto.CreateOpportunityRequest;
import ru.tramplin_itplanet.tramplin.web.dto.OpportunityCardResponse;
import ru.tramplin_itplanet.tramplin.web.dto.OpportunityMiniCardResponse;
import ru.tramplin_itplanet.tramplin.web.mapper.CreateOpportunityRequestMapper;
import ru.tramplin_itplanet.tramplin.web.mapper.OpportunityCardMapper;
import ru.tramplin_itplanet.tramplin.web.mapper.OpportunityMiniCardMapper;

import java.util.List;

@Tag(name = "Opportunities", description = "Career opportunities: vacancies, internships, mentorships, and events")
@RestController
@RequestMapping("/opportunities")
public class OpportunityController {

    private static final Logger log = LoggerFactory.getLogger(OpportunityController.class);

    private final OpportunityService opportunityService;

    public OpportunityController(OpportunityService opportunityService) {
        this.opportunityService = opportunityService;
    }

    @Operation(
            summary = "Get mini-cards for home page",
            description = "Returns opportunity mini-cards with first media, title, description, employer name, format, and first three tags."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mini-cards returned successfully")
    })
    @GetMapping("/mini-cards")
    public ResponseEntity<List<OpportunityMiniCardResponse>> getMiniCards() {
        log.info("GET /opportunities/mini-cards");
        List<OpportunityMiniCardResponse> response = opportunityService.findActiveMiniCards().stream()
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
            summary = "Create a new opportunity card",
            description = "Creates a vacancy, internship, mentorship, or career event. Returns the full created card with its generated ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Opportunity created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed — required fields missing or invalid",
                    content = @Content(schema = @Schema(example = "{\"status\":400,\"errors\":[\"title: must not be blank\"]}"))),
            @ApiResponse(responseCode = "404", description = "Referenced employer not found",
                    content = @Content(schema = @Schema(example = "{\"status\":404,\"error\":\"Employer not found with id: 5\"}")))
    })
    @PostMapping
    public ResponseEntity<OpportunityCardResponse> create(@Valid @RequestBody CreateOpportunityRequest request) {
        log.info("POST /opportunities: title={}, type={}, employerId={}", request.title(), request.type(), request.employerId());
        OpportunityCardResponse response = OpportunityCardMapper.toResponse(
                opportunityService.create(CreateOpportunityRequestMapper.toCommand(request))
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
