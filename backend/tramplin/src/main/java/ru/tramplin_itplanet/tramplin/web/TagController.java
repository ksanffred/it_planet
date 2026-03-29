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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.tramplin_itplanet.tramplin.domain.service.TagService;
import ru.tramplin_itplanet.tramplin.web.dto.CreateTagRequest;
import ru.tramplin_itplanet.tramplin.web.dto.TagResponse;
import ru.tramplin_itplanet.tramplin.web.dto.UpdateTagRequest;
import ru.tramplin_itplanet.tramplin.web.mapper.CreateTagRequestMapper;
import ru.tramplin_itplanet.tramplin.web.mapper.TagMapper;
import ru.tramplin_itplanet.tramplin.web.mapper.UpdateTagRequestMapper;

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

    @Operation(summary = "Update tag by ID (curator only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tag updated"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "403", description = "Current user is not a curator"),
            @ApiResponse(responseCode = "404", description = "Tag not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    public ResponseEntity<TagResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTagRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        ensureCuratorRole(authentication);
        log.info("PUT /tags/{}: name={}, category={}", id, request.name(), request.category());
        TagResponse response = TagMapper.toResponse(tagService.update(id, UpdateTagRequestMapper.toCommand(request)));
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete tag by ID (curator only)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tag deleted"),
            @ApiResponse(responseCode = "403", description = "Current user is not a curator"),
            @ApiResponse(responseCode = "404", description = "Tag not found")
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        ensureCuratorRole(authentication);
        log.info("DELETE /tags/{}", id);
        tagService.delete(id);
        return ResponseEntity.noContent().build();
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
