package com.kulturman.irembotest.api.controllers;

import com.kulturman.irembotest.api.dto.ResourceId;
import com.kulturman.irembotest.domain.application.CertificateService;
import com.kulturman.irembotest.domain.application.GenerateCertificateRequest;
import com.kulturman.irembotest.domain.entities.Certificate;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
