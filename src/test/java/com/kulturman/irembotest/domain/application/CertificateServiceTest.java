package com.kulturman.irembotest.domain.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kulturman.irembotest.domain.application.entities.Certificate;
import com.kulturman.irembotest.domain.application.entities.Template;
import com.kulturman.irembotest.domain.entities.CertificateStatus;
import com.kulturman.irembotest.domain.exceptions.TemplateNotFoundException;
import com.kulturman.irembotest.domain.ports.TenancyProvider;
import com.kulturman.irembotest.infrastructure.persistence.InMemoryCertificateRepository;
import com.kulturman.irembotest.infrastructure.persistence.InMemoryTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CertificateServiceTest {
    @Mock
    TenancyProvider tenancyProvider;

    InMemoryTemplateRepository templateRepository;
    InMemoryCertificateRepository certificateRepository;
    CertificateService certificateService;
    ObjectMapper objectMapper;
    UUID tenantId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        templateRepository = new InMemoryTemplateRepository();
        certificateRepository = new InMemoryCertificateRepository();
        objectMapper = new ObjectMapper();
        certificateService = new CertificateService(tenancyProvider, templateRepository, certificateRepository, objectMapper);
        when(tenancyProvider.getCurrentTenantId()).thenReturn(tenantId);
    }

    @Test
    void shouldGenerateCertificateWithQueuedStatus() {
        var templateId = UUID.randomUUID();
        templateRepository.addTemplate(
            Template.builder()
                .id(templateId)
                .tenantId(tenantId)
                .name("Certificate Template")
                .content("Certificate for {name}")
                .variables("[\"name\"]")
                .build()
        );

        var request = GenerateCertificateRequest.builder()
            .templateId(templateId)
            .variables(Map.of("name", "Arnaud BAKYONO"))
            .build();

        Certificate certificate = certificateService.generateCertificate(request);

        assertNotNull(certificate.getId());
        assertEquals(templateId, certificate.getTemplateId());
        assertEquals(CertificateStatus.QUEUED, certificate.getStatus());
        assertEquals(tenantId, certificate.getTenantId());
        assertTrue(certificate.getVariables().contains("Arnaud BAKYONO"));

        assertEquals(1, certificateRepository.getSavedCertificates().size());
        Certificate savedCertificate = certificateRepository.getSavedCertificates().get(0);
        assertEquals(certificate.getId(), savedCertificate.getId());
    }

    @Test
    void shouldFailIfTemplateDoesNotBelongToCurrentTenant() {
        var templateId = UUID.randomUUID();
        var differentTenantId = UUID.randomUUID();

        templateRepository.addTemplate(
            Template.builder()
                .id(templateId)
                .tenantId(differentTenantId)
                .name("Certificate Template")
                .content("Certificate for {name}")
                .variables("[\"name\"]")
                .build()
        );

        var request = GenerateCertificateRequest.builder()
            .templateId(templateId)
            .variables(Map.of("name", "Arnaud BAKYONO"))
            .build();

        assertThrows(TemplateNotFoundException.class, () -> certificateService.generateCertificate(request));
    }
}
