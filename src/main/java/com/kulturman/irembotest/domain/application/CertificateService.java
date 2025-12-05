package com.kulturman.irembotest.domain.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kulturman.irembotest.domain.entities.Certificate;
import com.kulturman.irembotest.domain.entities.CertificateStatus;
import com.kulturman.irembotest.domain.exceptions.CertificateFileNotFoundException;
import com.kulturman.irembotest.domain.exceptions.CertificateGenerationFailedException;
import com.kulturman.irembotest.domain.exceptions.CertificateNotFoundException;
import com.kulturman.irembotest.domain.exceptions.CertificateNotReadyException;
import com.kulturman.irembotest.domain.exceptions.TemplateNotFoundException;
import com.kulturman.irembotest.domain.ports.CertificateQueue;
import com.kulturman.irembotest.domain.ports.CertificateRepository;
import com.kulturman.irembotest.domain.ports.FileStorage;
import com.kulturman.irembotest.domain.ports.TenancyProvider;
import com.kulturman.irembotest.domain.ports.TemplateRepository;
import com.kulturman.irembotest.infrastructure.util.TokenGenerator;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
@Service
public class CertificateService {
    private final TenancyProvider tenancyProvider;
    private final TemplateRepository templateRepository;
    private final CertificateRepository certificateRepository;
    private final CertificateQueue certificateQueue;
    private final ObjectMapper objectMapper;
    private final TokenGenerator tokenGenerator;
    private final FileStorage fileStorage;

    public Certificate generateCertificate(GenerateCertificateRequest request) {
        var tenantId = tenancyProvider.getCurrentTenantId();

        var template = templateRepository.findByIdAndTenantId(request.getTemplateId(), tenantId).orElseThrow(() -> new TemplateNotFoundException("Template not found"));

        String variablesJson = getVariablesJson(request);

        var certificate = Certificate.builder()
            .id(UUID.randomUUID())
            .template(template)
            .variables(variablesJson)
            .status(CertificateStatus.QUEUED)
            .tenantId(tenantId)
            .downloadToken(tokenGenerator.generateSecureToken())
            .build();

        certificateRepository.save(certificate);
        certificateQueue.publish(certificate);

        return certificate;
    }

    public CertificateDownload getCertificateForDownload(String token) {
        Certificate certificate = certificateRepository.findByDownloadToken(token).orElseThrow(() -> new CertificateNotFoundException("Certificate not found"));

        if (certificate.getStatus() == CertificateStatus.FAILED) {
            throw new CertificateGenerationFailedException("Certificate generation failed");
        }

        if (certificate.getStatus() != CertificateStatus.COMPLETED) {
            throw new CertificateNotReadyException("Certificate is still being generated");
        }

        if (certificate.getFilePath() == null) {
            throw new CertificateFileNotFoundException("Certificate file path not found");
        }

        try {
            byte[] pdfContent = fileStorage.retrieve(certificate.getFilePath());
            return CertificateDownload.builder()
                .pdfContent(pdfContent)
                .certificateId(certificate.getId())
                .build();
        } catch (IOException e) {
            throw new CertificateFileNotFoundException("Failed to retrieve certificate file", e);
        }
    }

    public Page<Certificate> getCertificatesByTemplate(UUID templateId, Pageable pageable) {
        var tenantId = tenancyProvider.getCurrentTenantId();
        templateRepository.findByIdAndTenantId(templateId, tenantId).orElseThrow(() -> new TemplateNotFoundException("Template not found"));
        return certificateRepository.findByTemplateIdAndTenantId(templateId, tenantId, pageable);
    }

    private String getVariablesJson(GenerateCertificateRequest request) {
        List<Variable> variableList = request.getVariables().entrySet().stream()
            .map(entry -> Variable.builder()
                .key(entry.getKey())
                .value(entry.getValue())
                .build())
            .collect(Collectors.toList());

        try {
            return objectMapper.writeValueAsString(variableList);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize variables", e);
        }
    }
}
