package com.kulturman.irembotest.api.controllers;

import com.kulturman.irembotest.AbstractIntegrationTest;
import com.kulturman.irembotest.domain.application.CertificateService;
import com.kulturman.irembotest.domain.application.GenerateCertificateRequest;
import com.kulturman.irembotest.domain.entities.Certificate;
import com.kulturman.irembotest.domain.entities.CertificateStatus;
import com.kulturman.irembotest.domain.entities.Template;
import com.kulturman.irembotest.domain.entities.User;
import com.kulturman.irembotest.infrastructure.persistence.CertificateDb;
import com.kulturman.irembotest.infrastructure.persistence.TemplateDb;
import com.kulturman.irembotest.infrastructure.persistence.UserDb;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

public class CertificateGenerationE2ETest extends AbstractIntegrationTest {
    @Autowired
    private UserDb userRepository;

    @Autowired
    private TemplateDb templateRepository;

    @Autowired
    private CertificateDb certificateRepository;

    @Autowired
    private CertificateService certificateService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private Template template;

    @BeforeEach
    void setUp() {
        UUID tenantId = UUID.randomUUID();
        User user = User.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .email("test@example.com")
                .password(passwordEncoder.encode("password"))
                .name("Test User")
                .build();

        userRepository.save(user);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user,
            null,
            null
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        template = Template.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .name("Certificate of Achievement")
                .content("<html><body>Certificate for {name} - Course: {course}</body></html>")
                .variables("[\"name\", \"course\"]")
                .build();

        templateRepository.save(template);
    }

    @Test
    void shouldGenerateCertificateThroughRabbitMQ() {
        Map<String, String> variables = new HashMap<>();
        variables.put("name", "Arnaud");
        variables.put("course", "Spring Boot Development");

        GenerateCertificateRequest request = GenerateCertificateRequest.builder()
                .templateId(template.getId())
                .variables(variables)
                .build();

        Certificate certificate = certificateService.generateCertificate(request);

        assertNotNull(certificate);
        assertEquals(CertificateStatus.QUEUED, certificate.getStatus());
        assertNull(certificate.getFilePath());

        UUID certificateId = certificate.getId();

        await()
            .atMost(10, TimeUnit.SECONDS)
            .pollInterval(Duration.ofMillis(500))
            .untilAsserted(() -> {
                Certificate updatedCert = certificateRepository.findById(certificateId)
                        .orElseThrow(() -> new AssertionError("Certificate not found"));

                assertThat(updatedCert.getStatus())
                        .as("Certificate should be completed")
                        .isEqualTo(CertificateStatus.COMPLETED);
            });

        Certificate completedCertificate = certificateRepository.findById(certificateId).orElseThrow(() -> new AssertionError("Certificate not found"));
        assertEquals(CertificateStatus.COMPLETED, completedCertificate.getStatus());
        assertNotNull(completedCertificate.getFilePath());
    }
}
