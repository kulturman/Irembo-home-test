package com.kulturman.irembotest.domain.application;

import com.kulturman.irembotest.domain.application.entities.Certificate;
import com.kulturman.irembotest.domain.application.entities.Template;
import com.kulturman.irembotest.domain.entities.CertificateStatus;
import com.kulturman.irembotest.domain.ports.PdfGenerator;
import com.kulturman.irembotest.infrastructure.persistence.InMemoryCertificateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CertificateGenerationJobTest {
    InMemoryCertificateRepository certificateRepository;
    CertificateGenerationJob certificateGenerationJob;
    Certificate certificate;
    UUID tenantId = UUID.randomUUID();
    UUID certificatedWithPlaceHoldersId = UUID.randomUUID();
    UUID certificateWithoutPlaceHoldersId = UUID.randomUUID();

    @Mock
    PdfGenerator pdfGenerator;

    @BeforeEach
    void setUp() {
        certificateRepository = new InMemoryCertificateRepository();
        certificateGenerationJob = new CertificateGenerationJob(certificateRepository, pdfGenerator);
    }

    @Test
    void shouldProcessCertificateSuccessfullyWithPlaceHolders() {
        certificateRepository.save(
            Certificate.builder()
                .id(certificatedWithPlaceHoldersId)
                .status(CertificateStatus.QUEUED)
                .template(
                    Template.builder()
                        .id(UUID.randomUUID())
                        .tenantId(tenantId)
                        .name("Certificate Template")
                        .content("<h1>Hello guys, I am {name}, my dream company is {dreamCompany}</h1>")
                        .variables("[\"name\", \"dreamCompany\"]").build()
                )
                .variables("[{\"key\":\"name\",\"value\":\"Arnaud\"}, {\"key\":\"dreamCompany\",\"value\":\"Irembo\"}]")
                .tenantId(tenantId)
                .build()
        );
        certificateGenerationJob.execute(certificatedWithPlaceHoldersId);
        verify(pdfGenerator).generatePdf("<h1>Hello guys, I am Arnaud, my dream company is Irembo</h1>");
        Certificate updatedCertificate = certificateRepository.savedCertificates.getFirst();
        assertEquals(CertificateStatus.COMPLETED, updatedCertificate.getStatus());
    }

    @Test
    void shouldProcessCertificateSuccessfullyWithNoPlaceHolders() {
        certificateRepository.save(
            Certificate.builder()
                .id(certificateWithoutPlaceHoldersId)
                .status(CertificateStatus.QUEUED)
                .template(
                    Template.builder()
                        .id(UUID.randomUUID())
                        .tenantId(tenantId)
                        .name("Certificate Template")
                        .content("<h1>Hello guys</h1>")
                        .variables("[\"name\", \"dreamCompany\"]").build()
                )
                .variables("[]")
                .tenantId(tenantId)
                .build()
        );
        certificateGenerationJob.execute(certificateWithoutPlaceHoldersId);
        verify(pdfGenerator).generatePdf("<h1>Hello guys</h1>");
        Certificate updatedCertificate = certificateRepository.savedCertificates.getFirst();
        assertEquals(CertificateStatus.COMPLETED, updatedCertificate.getStatus());
    }
}
