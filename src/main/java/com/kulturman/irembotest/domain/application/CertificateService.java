package com.kulturman.irembotest.domain.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kulturman.irembotest.domain.entities.Certificate;
import com.kulturman.irembotest.domain.entities.CertificateStatus;
import com.kulturman.irembotest.domain.exceptions.TemplateNotFoundException;
import com.kulturman.irembotest.domain.ports.CertificateQueue;
import com.kulturman.irembotest.domain.ports.CertificateRepository;
import com.kulturman.irembotest.domain.ports.TenancyProvider;
import com.kulturman.irembotest.domain.ports.TemplateRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

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
            .build();

        certificateRepository.save(certificate);
        certificateQueue.publish(certificate);

        return certificate;
    }

    private String getVariablesJson(GenerateCertificateRequest request) {
        List<Variable> variableList = request.getVariables().entrySet().stream()
            .map(entry -> Variable.builder()
                .key(entry.getKey())
                .value(entry.getValue())
                .build())
            .collect(Collectors.toList());

        // Serialize to JSON array: [{"key": "name", "value": "John"}]
        try {
            return objectMapper.writeValueAsString(variableList);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize variables", e);
        }
    }
}
