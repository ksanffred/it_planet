package ru.tramplin_itplanet.tramplin.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.tramplin_itplanet.tramplin.domain.service.TagService;
import ru.tramplin_itplanet.tramplin.web.dto.CreateTagRequest;
import ru.tramplin_itplanet.tramplin.web.dto.TagResponse;
import ru.tramplin_itplanet.tramplin.web.mapper.CreateTagRequestMapper;
import ru.tramplin_itplanet.tramplin.web.mapper.TagMapper;

import java.util.List;

@io.swagger.v3.oas.annotations.tags.Tag(name = "Tags", description = "System tags for opportunities")
@RestController
@RequestMapping("/tags")
public class TagController {

    private static final Logger log = LoggerFactory.getLogger(TagController.class);

    private final TagService tagService;

    public TagController(TagService tagService) {
        this.tagService = tagService;
    }

    @Operation(summary = "Get all tags", description = "Returns all system tags used to classify opportunities.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tags returned successfully")
    })
    @GetMapping
    public ResponseEntity<List<TagResponse>> getAll() {
        log.info("GET /tags");
        List<TagResponse> response = tagService.findAll().stream().map(TagMapper::toResponse).toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Create a new tag", description = "Creates a new system tag used to classify opportunities.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tag created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(example = "{\"status\":400,\"errors\":[\"name: must not be blank\"]}"))),
            @ApiResponse(responseCode = "409", description = "Tag with this name already exists",
                    content = @Content(schema = @Schema(example = "{\"status\":409,\"error\":\"Tag already exists with name: Docker\"}")))
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<TagResponse> create(@Valid @RequestBody CreateTagRequest request) {
        log.info("POST /tags: name={}, category={}", request.name(), request.category());
        TagResponse response = TagMapper.toResponse(tagService.create(CreateTagRequestMapper.toCommand(request)));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
