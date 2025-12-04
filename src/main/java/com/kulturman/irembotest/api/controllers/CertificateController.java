package com.kulturman.irembotest.api.controllers;

import com.kulturman.irembotest.api.dto.CertificateResponse;
import com.kulturman.irembotest.api.dto.ResourceId;
import com.kulturman.irembotest.domain.application.CertificateService;
import com.kulturman.irembotest.domain.application.GenerateCertificateRequest;
import com.kulturman.irembotest.domain.entities.Certificate;
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
public class CertificateController {
    private final CertificateService certificateService;

    @PostMapping
    public ResponseEntity<ResourceId> generateCertificate(@Valid @RequestBody GenerateCertificateRequest request) {
        Certificate certificate = certificateService.generateCertificate(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ResourceId(certificate.getId().toString()));
    }

    @GetMapping("/by-template/{templateId}")
    public ResponseEntity<Page<CertificateResponse>> getCertificatesByTemplate(
        @PathVariable UUID templateId,
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
