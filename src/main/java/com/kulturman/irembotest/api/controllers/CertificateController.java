package com.kulturman.irembotest.api.controllers;

import com.kulturman.irembotest.api.dto.CertificateResponse;
import com.kulturman.irembotest.api.dto.ResourceId;
import com.kulturman.irembotest.domain.application.CertificateService;
import com.kulturman.irembotest.api.dto.GenerateCertificateRequest;
import com.kulturman.irembotest.domain.entities.Certificate;
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
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/certificates")
@AllArgsConstructor
@Tag(name = "Certificates", description = "Certificate generation and management endpoints for creating and retrieving certificates")
public class CertificateController {
    private final CertificateService certificateService;

    @PostMapping
    @Operation(
        summary = "Generate a new certificate",
        description = "Generate a new certificate from a template by providing the template ID and variable values. The system will replace template placeholders with the provided variables and generate a PDF certificate."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Certificate generated successfully, returns the new certificate ID and download token",
            content = @Content(schema = @Schema(implementation = ResourceId.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request body, validation failed, or missing required variables",
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
    public ResponseEntity<ResourceId> generateCertificate(@Valid @RequestBody GenerateCertificateRequest request) {
        Certificate certificate = certificateService.generateCertificate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ResourceId(certificate.getId().toString()));
    }

    @GetMapping("/by-template/{templateId}")
    @Operation(
        summary = "Get certificates by template",
        description = "Retrieve a paginated list of all certificates generated from a specific template. Results are sorted by creation date in descending order by default."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Certificates retrieved successfully",
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
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Template not found with the given ID",
            content = @Content
        )
    })
    public ResponseEntity<Page<CertificateResponse>> getCertificatesByTemplate(
        @Parameter(description = "Template UUID", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable UUID templateId,
        @Parameter(description = "Pagination and sorting parameters", example = "page=0&size=20&sort=createdAt,desc")
        @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<Certificate> certificates = certificateService.getCertificatesByTemplate(templateId, pageable);
        Page<CertificateResponse> response = certificates.map(this::toCertificateResponse);
        return ResponseEntity.ok(response);
    }

    private CertificateResponse toCertificateResponse(Certificate certificate) {
        return CertificateResponse.builder()
            .id(certificate.getId())
            .templateId(certificate.getTemplate().getId())
            .templateName(certificate.getTemplate().getName())
            .variables(certificate.getVariables())
            .status(certificate.getStatus())
            .downloadToken(certificate.getDownloadToken())
            .createdAt(certificate.getCreatedAt())
            .updatedAt(certificate.getUpdatedAt())
            .build();
    }
}
