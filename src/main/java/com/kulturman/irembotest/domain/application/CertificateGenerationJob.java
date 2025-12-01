package com.kulturman.irembotest.domain.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kulturman.irembotest.domain.application.entities.Certificate;
import com.kulturman.irembotest.domain.entities.CertificateStatus;
import com.kulturman.irembotest.domain.ports.CertificateRepository;
import com.kulturman.irembotest.domain.ports.FileStorage;
import com.kulturman.irembotest.domain.ports.PdfGenerator;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.util.UUID;

@AllArgsConstructor
public class CertificateGenerationJob {
    private final CertificateRepository certificateRepository;
    private final PdfGenerator pdfGenerator;
    private final FileStorage fileStorage;

    public void execute(UUID certificateId) {
        var certificate = certificateRepository.findById(certificateId).orElseThrow(() -> new RuntimeException("Certificate not found"));

        try {
            certificate.setStatus(CertificateStatus.PROCESSING);
            certificateRepository.save(certificate);

            var processedTemplate = replaceVariables(certificate);
            var pdfContent = pdfGenerator.generatePdf(processedTemplate);

            var fileName = generateFileName(certificateId);
            var filePath = fileStorage.store(fileName, pdfContent);

            certificate.setFilePath(filePath);
            certificate.setStatus(CertificateStatus.COMPLETED);
            certificateRepository.save(certificate);
        } catch (IOException e) {
            certificate.setStatus(CertificateStatus.FAILED);
            certificateRepository.save(certificate);
            throw new RuntimeException("Failed to store certificate file", e);
        }
    }

    private String generateFileName(UUID certificateId) {
        return String.format("cert-%s.pdf", certificateId);
    }

    private String replaceVariables(Certificate certificate) {
        var templateContent = certificate.getTemplate().getContent();
        try {
            var variables = certificate.getVariablesAsList();

            for(Variable variable : variables) {
                templateContent = templateContent.replace("{" + variable.getKey() + "}", variable.getValue());
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return templateContent;
    }
}
