package com.kulturman.irembotest.api.controllers;

import com.kulturman.irembotest.AbstractIntegrationTest;
import com.kulturman.irembotest.domain.entities.Certificate;
import com.kulturman.irembotest.domain.entities.CertificateStatus;
import com.kulturman.irembotest.domain.entities.Template;
import com.kulturman.irembotest.domain.entities.User;
import com.kulturman.irembotest.infrastructure.persistence.CertificateDb;
import com.kulturman.irembotest.infrastructure.persistence.TemplateDb;
import com.kulturman.irembotest.infrastructure.persistence.UserDb;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CertificateControllerE2ETest extends AbstractIntegrationTest {
    @Autowired
    private UserDb userRepository;

    @Autowired
    private TemplateDb templateRepository;

    @Autowired
    private CertificateDb certificateRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UUID tenantA;
    private User userA;
    private String tokenA;
    private Template templateA;

    @BeforeEach
    void setUp() {
        tenantA = UUID.randomUUID();
        userA = User.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantA)
                .email("userA@example.com")
                .password(passwordEncoder.encode("password"))
                .name("User A")
                .build();

        userRepository.save(userA);

        tokenA = jwtService.generateToken(userA);

        templateA = Template.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantA)
                .name("Certificate of Achievement A")
                .content("<html><body>Certificate for {name} - Course: {course}</body></html>")
                .variables("[\"name\", \"course\"]")
                .build();

        templateRepository.save(templateA);
    }

    @Test
    void shouldGenerateCertificateSuccessfully() throws Exception {
        String requestBody = """
                {
                    "templateId": "%s",
                    "variables": {
                        "name": "Arnaud",
                        "course": "Spring Boot Development"
                    }
                }
                """.formatted(templateA.getId());

        String response = mockMvc.perform(
            post("/api/certificates")
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andReturn()
            .getResponse()
            .getContentAsString();

        UUID certificateId = UUID.fromString(objectMapper.readTree(response).get("id").asText());

        Certificate certificate = certificateRepository.findById(certificateId).orElseThrow();
        assertEquals(CertificateStatus.QUEUED, certificate.getStatus());
        assertNull(certificate.getFilePath());
        assertEquals(tenantA, certificate.getTenantId());

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

        Certificate completedCertificate = certificateRepository.findById(certificateId).orElseThrow();
        assertEquals(CertificateStatus.COMPLETED, completedCertificate.getStatus());
        assertNotNull(completedCertificate.getFilePath());
        assertTrue(completedCertificate.getFilePath().contains(".pdf"));
    }
}
