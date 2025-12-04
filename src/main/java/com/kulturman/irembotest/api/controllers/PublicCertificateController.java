package com.kulturman.irembotest.api.controllers;

import com.kulturman.irembotest.domain.application.CertificateDownload;
import com.kulturman.irembotest.domain.application.CertificateService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/certificates")
@AllArgsConstructor
public class PublicCertificateController {
    private final CertificateService certificateService;

    @GetMapping("/download/{token}")
    public ResponseEntity<byte[]> downloadCertificate(@PathVariable String token) {
        CertificateDownload download = certificateService.getCertificateForDownload(token);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "certificate-" + download.getCertificateId() + ".pdf");

        return ResponseEntity.ok()
            .headers(headers)
            .body(download.getPdfContent());
    }
}
