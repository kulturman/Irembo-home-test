package com.kulturman.irembotest.domain.application;

import com.kulturman.irembotest.domain.entities.Certificate;
import com.kulturman.irembotest.domain.entities.Template;
import com.kulturman.irembotest.domain.entities.CertificateStatus;
import com.kulturman.irembotest.domain.ports.FileStorage;
import com.kulturman.irembotest.domain.ports.PdfGenerator;
import com.kulturman.irembotest.infrastructure.persistence.InMemoryCertificateRepository;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CertificateGenerationJobTest {
    InMemoryCertificateRepository certificateRepository;
    CertificateGenerationJob certificateGenerationJob;
    UUID tenantId = UUID.randomUUID();
    UUID certificatedWithPlaceHoldersId = UUID.randomUUID();
    UUID certificateWithoutPlaceHoldersId = UUID.randomUUID();

    @Mock
    PdfGenerator pdfGenerator;

    @Mock
    FileStorage fileStorage;

    @BeforeEach
    void setUp() throws IOException {
        certificateRepository = new InMemoryCertificateRepository();
        certificateGenerationJob = new CertificateGenerationJob(
                certificateRepository,
                pdfGenerator,
                fileStorage,
                "http://localhost:8080"
        );
        lenient().when(pdfGenerator.generatePdf(anyString())).thenReturn(createValidPdfBytes());
    }

    private byte[] createValidPdfBytes() throws IOException {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            document.addPage(new PDPage());
            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }

    @Test
    void shouldProcessCertificateSuccessfullyWithPlaceHolders() {
        certificateRepository.save(
            Certificate.builder()
                .id(certificatedWithPlaceHoldersId)
                .status(CertificateStatus.QUEUED)
                .downloadToken(UUID.randomUUID().toString())
                .template(
                    Template.builder()
                        .id(UUID.randomUUID())
                        .tenantId(tenantId)
                        .name("Certificate Template")
                        .content("<h1>Hello guys, I am {name}, my dream company is {dreamCompany}</h1>")
                        .variables("[\"name\", \"dreamCompany\"]").build()
                )
                .variables("{\"name\":\"Arnaud\",\"dreamCompany\":\"Irembo\"}")
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
                .downloadToken(UUID.randomUUID().toString())
                .template(
                    Template.builder()
                        .id(UUID.randomUUID())
                        .tenantId(tenantId)
                        .name("Certificate Template")
                        .content("<h1>Hello guys</h1>")
                        .variables("[\"name\", \"dreamCompany\"]").build()
                )
                .variables("{}")
                .tenantId(tenantId)
                .build()
        );
        certificateGenerationJob.execute(certificateWithoutPlaceHoldersId);
        verify(pdfGenerator).generatePdf("<h1>Hello guys</h1>");
        Certificate updatedCertificate = certificateRepository.savedCertificates.getFirst();
        assertEquals(CertificateStatus.COMPLETED, updatedCertificate.getStatus());
    }

    @Test
    void shouldSaveFileAndStoreFilePathInCertificate() throws IOException {
        UUID certificateId = UUID.randomUUID();
        String expectedFilePath = certificateId + "/cert-" + certificateId + ".pdf";
        byte[] validPdfBytes = createValidPdfBytes();

        when(pdfGenerator.generatePdf(anyString())).thenReturn(validPdfBytes);
        when(fileStorage.store(anyString(), any(byte[].class))).thenReturn(expectedFilePath);

        certificateRepository.save(
            Certificate.builder()
                .id(certificateId)
                .status(CertificateStatus.QUEUED)
                .downloadToken(UUID.randomUUID().toString())
                .template(
                    Template.builder()
                        .id(UUID.randomUUID())
                        .tenantId(tenantId)
                        .name("Certificate Template")
                        .content("<h1>Test Certificate</h1>")
                        .variables("{}")
                        .build()
                )
                .variables("{}")
                .tenantId(tenantId)
                .build()
        );

        certificateGenerationJob.execute(certificateId);

        verify(fileStorage).store(eq(expectedFilePath), any(byte[].class));

        Certificate updatedCertificate = certificateRepository.findById(certificateId).orElseThrow();
        assertNotNull(updatedCertificate.getFilePath());
        assertEquals(expectedFilePath, updatedCertificate.getFilePath());
        assertEquals(CertificateStatus.COMPLETED, updatedCertificate.getStatus());
    }
}
