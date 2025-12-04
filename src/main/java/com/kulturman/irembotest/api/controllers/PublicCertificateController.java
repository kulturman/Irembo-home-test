package com.kulturman.irembotest.api.controllers;

import com.kulturman.irembotest.domain.application.CertificateDownload;
import com.kulturman.irembotest.domain.application.CertificateService;
import com.kulturman.irembotest.domain.entities.Certificate;
import com.kulturman.irembotest.domain.exceptions.CertificateNotFoundException;
import com.kulturman.irembotest.domain.ports.CertificateRepository;
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
public class PublicCertificateController {
    private final CertificateService certificateService;
    private final CertificateRepository certificateRepository;

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

    @GetMapping("/verify/{id}")
    public String verifyCertificate(@PathVariable UUID id, Model model) {
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
