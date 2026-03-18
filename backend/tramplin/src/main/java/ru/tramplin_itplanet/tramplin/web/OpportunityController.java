package ru.tramplin_itplanet.tramplin.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.tramplin_itplanet.tramplin.domain.service.OpportunityService;
import ru.tramplin_itplanet.tramplin.web.dto.OpportunityCardResponse;
import ru.tramplin_itplanet.tramplin.web.mapper.OpportunityCardMapper;

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
        OpportunityCardResponse response = OpportunityCardMapper.toResponse(
                opportunityService.getById(id)
        );
        return ResponseEntity.ok(response);
    }
}
