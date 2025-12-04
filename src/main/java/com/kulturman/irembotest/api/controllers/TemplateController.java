package com.kulturman.irembotest.api.controllers;

import com.kulturman.irembotest.api.dto.ResourceId;
import com.kulturman.irembotest.api.dto.TemplateResponse;
import com.kulturman.irembotest.domain.application.CreateTemplateRequest;
import com.kulturman.irembotest.domain.application.TemplateService;
import com.kulturman.irembotest.domain.application.UpdateTemplateRequest;
import com.kulturman.irembotest.domain.entities.Template;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/templates")
@AllArgsConstructor
@Tag(name = "Templates", description = "Certificate template management endpoints for creating, updating, and retrieving templates")
public class TemplateController {
    private final TemplateService templateService;

    @GetMapping
    @Operation(
        summary = "Get all templates",
        description = "Retrieve a paginated list of all certificate templates. Supports sorting and pagination parameters."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Templates retrieved successfully",
            content = @Content(schema = @Schema(implementation = Page.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - valid JWT token required",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - insufficient permissions",
            content = @Content
        )
    })
    public ResponseEntity<Page<TemplateResponse>> getTemplates(
        @Parameter(description = "Pagination and sorting parameters", example = "page=0&size=20&sort=name,asc")
        Pageable pageable
    ) {
        return ResponseEntity.ok(templateService.getTemplates(pageable));
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get template by ID",
        description = "Retrieve a specific certificate template by its unique identifier"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Template found and returned successfully",
            content = @Content(schema = @Schema(implementation = TemplateResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - valid JWT token required",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - insufficient permissions",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Template not found with the given ID",
            content = @Content
        )
    })
    public ResponseEntity<TemplateResponse> getTemplate(
        @Parameter(description = "Template UUID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable UUID id
    ) {
        return ResponseEntity.ok(templateService.getTemplate(id));
    }

    @PostMapping
    @Operation(
        summary = "Create a new template",
        description = "Create a new certificate template with name and content. Template content supports variable placeholders using {{variableName}} syntax."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Template created successfully, returns the new template ID",
            content = @Content(schema = @Schema(implementation = ResourceId.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request body or validation failed",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - valid JWT token required",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - insufficient permissions",
            content = @Content
        )
    })
    public ResponseEntity<ResourceId> createTemplate(@Valid @RequestBody CreateTemplateRequest request) {
        Template template = templateService.createTemplate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ResourceId(template.getId().toString()));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update an existing template",
        description = "Update an existing certificate template's name and/or content. Template content supports variable placeholders using {{variableName}} syntax."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Template updated successfully",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request body or validation failed",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - valid JWT token required",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - insufficient permissions",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Template not found with the given ID",
            content = @Content
        )
    })
    public ResponseEntity<Void> updateTemplate(
        @Parameter(description = "Template UUID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable UUID id,
        @Valid @RequestBody UpdateTemplateRequest request
    ) {
        templateService.updateTemplate(id, request);
        return ResponseEntity.noContent().build();
    }
}
