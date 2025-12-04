package com.kulturman.irembotest.api.controllers;

import com.kulturman.irembotest.domain.application.CertificateDownload;
import com.kulturman.irembotest.domain.application.CertificateService;
import com.kulturman.irembotest.domain.entities.Certificate;
import com.kulturman.irembotest.domain.exceptions.CertificateNotFoundException;
import com.kulturman.irembotest.domain.ports.CertificateRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/api/public/certificates")
@AllArgsConstructor
@Tag(name = "Public Certificates", description = "Public endpoints for certificate download and verification without authentication")
public class PublicCertificateController {
    private final CertificateService certificateService;
    private final CertificateRepository certificateRepository;

    @GetMapping("/download/{token}")
    @Operation(
        summary = "Download certificate PDF",
        description = "Download a certificate PDF file using a unique download token. No authentication required - the token serves as authorization."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Certificate PDF file returned successfully",
            content = @Content(
                mediaType = "application/pdf",
                schema = @Schema(type = "string", format = "binary")
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Certificate not found or invalid download token",
            content = @Content
        )
    })
    @SecurityRequirements
    public ResponseEntity<byte[]> downloadCertificate(
        @Parameter(description = "Unique download token for the certificate", example = "abc123def456")
        @PathVariable String token
    ) {
        CertificateDownload download = certificateService.getCertificateForDownload(token);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "certificate-" + download.getCertificateId() + ".pdf");

        return ResponseEntity.ok()
            .headers(headers)
            .body(download.getPdfContent());
    }

    @GetMapping("/verify/{id}")
    @Operation(
        summary = "Verify certificate authenticity",
        description = "Verify a certificate's authenticity by its ID and view its details. Returns an HTML page showing certificate information if found. No authentication required."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Certificate verification page returned (HTML view)",
            content = @Content(
                mediaType = "text/html",
                schema = @Schema(type = "string")
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Certificate not found - verification page shows 'not found' message",
            content = @Content(
                mediaType = "text/html",
                schema = @Schema(type = "string")
            )
        )
    })
    @SecurityRequirements
    public String verifyCertificate(
        @Parameter(description = "Certificate UUID to verify", example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable UUID id,
        Model model
    ) {
        try {
            Certificate certificate = certificateRepository.findById(id).orElseThrow(() -> new CertificateNotFoundException("Certificate not found"));
            String processedContent = certificateService.getProcessedCertificateContent(certificate);

            model.addAttribute("certificate", certificate);
            model.addAttribute("processedContent", processedContent);
            model.addAttribute("templateName", certificate.getTemplate().getName());
            model.addAttribute("createdAt", certificate.getCreatedAt());
            model.addAttribute("status", certificate.getStatus());
            model.addAttribute("found", true);

            return "certificate-verify";
        } catch (CertificateNotFoundException e) {
            model.addAttribute("found", false);
            model.addAttribute("certificateId", id);
            return "certificate-verify";
        }
    }
}
