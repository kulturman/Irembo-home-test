package com.kulturman.irembotest.api.controllers;

import com.kulturman.irembotest.AbstractIntegrationTest;
import com.kulturman.irembotest.domain.entities.Certificate;
import com.kulturman.irembotest.domain.entities.CertificateStatus;
import com.kulturman.irembotest.domain.entities.User;
import com.kulturman.irembotest.fixtures.FixtureUtils;
import com.kulturman.irembotest.infrastructure.persistence.CertificateDb;
import com.kulturman.irembotest.infrastructure.persistence.UserDb;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

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

@TestExecutionListeners({
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class
})
@DatabaseSetup({"/fixtures/users.xml", "/fixtures/certificate-test-data.xml"})
public class CertificateControllerE2ETest extends AbstractIntegrationTest {

    @Autowired
    private UserDb userRepository;

    @Autowired
    private CertificateDb certificateRepository;

    @Test
    void shouldGenerateCertificateSuccessfully() throws Exception {
        User userA = userRepository.findById(FixtureUtils.USER_A_ID).orElseThrow();
        String tokenA = jwtService.generateToken(userA);

        String requestBody = """
                {
                    "templateId": "%s",
                    "variables": {}
                }
                """.formatted(FixtureUtils.TEMPLATE_A_ID);

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
        assertEquals(FixtureUtils.TENANT_A_ID, certificate.getTenantId());

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
        User userA = userRepository.findById(FixtureUtils.USER_A_ID).orElseThrow();
        String tokenA = jwtService.generateToken(userA);

        mockMvc.perform(
            get("/api/certificates/by-template/{templateId}", FixtureUtils.TEMPLATE_A_ID)
            .header("Authorization", "Bearer " + tokenA)
            .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(4))
        .andExpect(jsonPath("$.content[0].id").exists())
        .andExpect(jsonPath("$.content[0].templateId").value(FixtureUtils.TEMPLATE_A_ID.toString()))
        .andExpect(jsonPath("$.content[0].templateName").value("Certificate of Achievement A"))
        .andExpect(jsonPath("$.content[0].status").exists())
        .andExpect(jsonPath("$.content[0].downloadToken").exists())
        .andExpect(jsonPath("$.content[0].createdAt").exists())
        .andExpect(jsonPath("$.totalElements").value(4))
        .andExpect(jsonPath("$.totalPages").value(1))
        .andExpect(jsonPath("$.size").value(20));
    }


    @Test
    void shouldReturn404WhenTemplateNotFound() throws Exception {
        User userA = userRepository.findById(FixtureUtils.USER_A_ID).orElseThrow();
        String tokenA = jwtService.generateToken(userA);

        UUID nonExistentTemplateId = UUID.randomUUID();

        mockMvc.perform (
            get("/api/certificates/by-template/{templateId}", nonExistentTemplateId)
            .header("Authorization", "Bearer " + tokenA)
            .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isNotFound());
    }

    @Test
    void shouldNotAccessCertificatesFromDifferentTenant() throws Exception {
        User userB = userRepository.findById(FixtureUtils.USER_B_ID).orElseThrow();
        String tokenB = jwtService.generateToken(userB);

        mockMvc.perform(
            get("/api/certificates/by-template/{templateId}", FixtureUtils.TEMPLATE_A_ID)
            .header("Authorization", "Bearer " + tokenB)
            .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNotFound());
    }
}
