package com.kulturman.irembotest.api.controllers;

import com.kulturman.irembotest.AbstractIntegrationTest;
import com.kulturman.irembotest.domain.application.CertificateService;
import com.kulturman.irembotest.api.dto.GenerateCertificateRequest;
import com.kulturman.irembotest.domain.entities.Certificate;
import com.kulturman.irembotest.domain.entities.CertificateStatus;
import com.kulturman.irembotest.domain.entities.User;
import com.kulturman.irembotest.fixtures.FixtureUtils;
import com.kulturman.irembotest.infrastructure.persistence.CertificateDb;
import com.kulturman.irembotest.infrastructure.persistence.UserDb;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
import static org.junit.jupiter.api.Assertions.*;

@TestExecutionListeners({
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class
})
@DatabaseSetup({"/fixtures/users.xml", "/fixtures/certificate-test-data.xml"})
public class CertificateGenerationE2ETest extends AbstractIntegrationTest {

    @Autowired
    private UserDb userRepository;

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
    void shouldGenerateCertificateThroughRabbitMQ() {
        Map<String, String> variables = new HashMap<>();
        variables.put("name", "Arnaud");
        variables.put("course", "Spring Boot Development");

        GenerateCertificateRequest request = GenerateCertificateRequest.builder()
                .templateId(FixtureUtils.TEMPLATE_CERTIFICATE_GENERATION_ID)
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
