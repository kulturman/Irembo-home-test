package com.kulturman.irembotest.api.controllers;

import com.kulturman.irembotest.AbstractIntegrationTest;
import com.kulturman.irembotest.api.dto.GenerateCertificateRequest;
import com.kulturman.irembotest.domain.application.CertificateService;
import com.kulturman.irembotest.domain.entities.Certificate;
import com.kulturman.irembotest.domain.entities.CertificateStatus;
import com.kulturman.irembotest.domain.entities.Template;
import com.kulturman.irembotest.domain.entities.User;
import com.kulturman.irembotest.fixtures.FixtureUtils;
import com.kulturman.irembotest.infrastructure.persistence.CertificateDb;
import com.kulturman.irembotest.infrastructure.persistence.TemplateDb;
import com.kulturman.irembotest.infrastructure.persistence.UserDb;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestExecutionListeners({
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class
})
@DatabaseSetup({"/fixtures/users.xml", "/fixtures/certificate-test-data.xml"})
public class PublicCertificateDownloadE2ETest extends AbstractIntegrationTest {

    @Autowired
    private UserDb userRepository;

    @Autowired
    private TemplateDb templateRepository;

    @Autowired
    private CertificateDb certificateRepository;

    @Autowired
    private CertificateService certificateService;

    @BeforeEach
    void setUp() {
        User user = userRepository.findById(FixtureUtils.USER_A_ID).orElseThrow();
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            user,
            null,
            null
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void shouldDownloadCertificateWithValidTokenWithoutAuthentication() throws Exception {
        Map<String, String> variables = new HashMap<>();
        variables.put("studentName", "Arnaud BAKYONO");
        variables.put("courseName", "Java Programming");

        GenerateCertificateRequest request = GenerateCertificateRequest.builder()
                .templateId(FixtureUtils.TEMPLATE_PUBLIC_DOWNLOAD_ID)
                .variables(variables)
                .build();

        Certificate certificate = certificateService.generateCertificate(request);

        assertNotNull(certificate.getDownloadToken());
        assertEquals(CertificateStatus.QUEUED, certificate.getStatus());

        UUID certificateId = certificate.getId();
        String downloadToken = certificate.getDownloadToken();

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

        mockMvc.perform(
            get("/api/public/certificates/download/{token}", downloadToken)
        )
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_PDF))
        .andExpect(header().exists("Content-Disposition"))
        .andExpect(header().string("Content-Disposition",
            containsString("attachment")))
        .andExpect(header().string("Content-Disposition",
            containsString("certificate-" + certificateId + ".pdf")));
    }

    @Test
    void shouldReturn404WhenTokenNotFound() throws Exception {
        String invalidToken = "invalid-token-that-does-not-exist";

        mockMvc.perform(get("/api/public/certificates/download/{token}", invalidToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldReturn202WhenCertificateIsStillProcessing() throws Exception {
        Map<String, String> variables = new HashMap<>();
        variables.put("studentName", "Arnaud");
        variables.put("courseName", "Python Programming");

        GenerateCertificateRequest request = GenerateCertificateRequest.builder()
                .templateId(FixtureUtils.TEMPLATE_PUBLIC_DOWNLOAD_ID)
                .variables(variables)
                .build();

        Certificate certificate = certificateService.generateCertificate(request);
        String downloadToken = certificate.getDownloadToken();

        mockMvc.perform(get("/api/public/certificates/download/{token}", downloadToken))
                .andExpect(status().isAccepted());
    }

    @Test
    void shouldReturn410WhenCertificateGenerationFailed() throws Exception {
        Template template = templateRepository.findById(FixtureUtils.TEMPLATE_PUBLIC_DOWNLOAD_ID).orElseThrow();
        UUID tenantId = ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getTenantId();

        Certificate failedCertificate = Certificate.builder()
                .id(UUID.randomUUID())
                .template(template)
                .tenantId(tenantId)
                .variables("{}")
                .status(CertificateStatus.FAILED)
                .downloadToken("failed-cert-token")
                .build();

        certificateRepository.save(failedCertificate);

        mockMvc.perform(get("/api/public/certificates/download/{token}", "failed-cert-token"))
                .andExpect(status().isGone());
    }

    @Test
    void shouldGenerateUniqueDifferentTokensForDifferentCertificates() {
        Map<String, String> variables1 = new HashMap<>();
        variables1.put("studentName", "Alice");
        variables1.put("courseName", "Math");

        Map<String, String> variables2 = new HashMap<>();
        variables2.put("studentName", "Bob");
        variables2.put("courseName", "Science");

        GenerateCertificateRequest request1 = GenerateCertificateRequest.builder()
                .templateId(FixtureUtils.TEMPLATE_PUBLIC_DOWNLOAD_ID)
                .variables(variables1)
                .build();

        GenerateCertificateRequest request2 = GenerateCertificateRequest.builder()
                .templateId(FixtureUtils.TEMPLATE_PUBLIC_DOWNLOAD_ID)
                .variables(variables2)
                .build();

        Certificate cert1 = certificateService.generateCertificate(request1);
        Certificate cert2 = certificateService.generateCertificate(request2);

        assertNotNull(cert1.getDownloadToken());
        assertNotNull(cert2.getDownloadToken());
        assertNotEquals(cert1.getDownloadToken(), cert2.getDownloadToken());
    }
}
