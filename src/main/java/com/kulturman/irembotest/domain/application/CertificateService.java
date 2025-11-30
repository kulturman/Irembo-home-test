package com.kulturman.irembotest.domain.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kulturman.irembotest.domain.application.entities.Certificate;
import com.kulturman.irembotest.domain.entities.CertificateStatus;
import com.kulturman.irembotest.domain.exceptions.TemplateNotFoundException;
import com.kulturman.irembotest.domain.ports.CertificateRepository;
import com.kulturman.irembotest.domain.ports.TenancyProvider;
import com.kulturman.irembotest.domain.ports.TemplateRepository;
import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public class CertificateService {
    private final TenancyProvider tenancyProvider;
    private final TemplateRepository templateRepository;
    private final CertificateRepository certificateRepository;
    private final ObjectMapper objectMapper;

    public Certificate generateCertificate(GenerateCertificateRequest request) {
        var tenantId = tenancyProvider.getCurrentTenantId();

        templateRepository.findByIdAndTenantId(request.getTemplateId(), tenantId).orElseThrow(() -> new TemplateNotFoundException("Template not found"));

        String variablesJson = getVariablesJson(request);

        var certificate = Certificate.builder()
            .id(UUID.randomUUID())
            .templateId(request.getTemplateId())
            .variables(variablesJson)
            .status(CertificateStatus.QUEUED)
            .tenantId(tenantId)
            .build();

        certificateRepository.save(certificate);

        return certificate;
    }

    private String getVariablesJson(GenerateCertificateRequest request) {
        String variablesJson;
        try {
            variablesJson = objectMapper.writeValueAsString(request.getVariables());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize variables", e);
        }
        return variablesJson;
    }
}
