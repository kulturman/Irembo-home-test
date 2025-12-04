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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
        certificateRepository.deleteAll();
        templateRepository.deleteAll();
        userRepository.deleteAll();

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

    @Test
    void shouldGetCertificatesByTemplateSuccessfully() throws Exception {
        // Create multiple certificates for templateA
        Certificate cert1 = Certificate.builder()
                .id(UUID.randomUUID())
                .template(templateA)
                .tenantId(tenantA)
                .variables("[{\"key\":\"name\",\"value\":\"John\"},{\"key\":\"course\",\"value\":\"Java\"}]")
                .status(CertificateStatus.COMPLETED)
                .downloadToken("token1")
                .build();

        Certificate cert2 = Certificate.builder()
                .id(UUID.randomUUID())
                .template(templateA)
                .tenantId(tenantA)
                .variables("[{\"key\":\"name\",\"value\":\"Jane\"},{\"key\":\"course\",\"value\":\"Python\"}]")
                .status(CertificateStatus.QUEUED)
                .downloadToken("token2")
                .build();

        Certificate cert3 = Certificate.builder()
                .id(UUID.randomUUID())
                .template(templateA)
                .tenantId(tenantA)
                .variables("[{\"key\":\"name\",\"value\":\"Bob\"},{\"key\":\"course\",\"value\":\"Go\"}]")
                .status(CertificateStatus.FAILED)
                .downloadToken("token3")
                .build();

        certificateRepository.save(cert1);
        certificateRepository.save(cert2);
        certificateRepository.save(cert3);

        // Get certificates for templateA
        mockMvc.perform(get("/api/certificates/by-template/{templateId}", templateA.getId())
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(3))
                .andExpect(jsonPath("$.content[0].id").exists())
                .andExpect(jsonPath("$.content[0].templateId").value(templateA.getId().toString()))
                .andExpect(jsonPath("$.content[0].templateName").value("Certificate of Achievement A"))
                .andExpect(jsonPath("$.content[0].status").exists())
                .andExpect(jsonPath("$.content[0].downloadToken").exists())
                .andExpect(jsonPath("$.content[0].createdAt").exists())
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.size").value(20));
    }


    @Test
    void shouldReturn404WhenTemplateNotFound() throws Exception {
        UUID nonExistentTemplateId = UUID.randomUUID();

        mockMvc.perform(get("/api/certificates/by-template/{templateId}", nonExistentTemplateId)
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotAccessCertificatesFromDifferentTenant() throws Exception {
        // Create a different tenant with their own template
        UUID tenantB = UUID.randomUUID();
        User userB = User.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantB)
                .email("userB@example.com")
                .password(passwordEncoder.encode("password"))
                .name("User B")
                .build();
        userRepository.save(userB);
        String tokenB = jwtService.generateToken(userB);

        Template templateB = Template.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantB)
                .name("Template B")
                .content("<html><body>Test</body></html>")
                .variables("[]")
                .build();
        templateRepository.save(templateB);

        // Create certificate for tenant A
        Certificate certA = Certificate.builder()
                .id(UUID.randomUUID())
                .template(templateA)
                .tenantId(tenantA)
                .variables("[]")
                .status(CertificateStatus.COMPLETED)
                .downloadToken("tokenA")
                .build();
        certificateRepository.save(certA);

        // User B tries to access certificates from tenant A's template
        mockMvc.perform(get("/api/certificates/by-template/{templateId}", templateA.getId())
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}
