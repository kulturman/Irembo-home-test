package com.kulturman.irembotest.domain.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kulturman.irembotest.domain.entities.Certificate;
import com.kulturman.irembotest.domain.entities.CertificateStatus;
import com.kulturman.irembotest.domain.ports.CertificateRepository;
import com.kulturman.irembotest.domain.ports.FileStorage;
import com.kulturman.irembotest.domain.ports.PdfGenerator;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@AllArgsConstructor
@Service
@Transactional
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
        } catch (Exception e) {
            certificate.setStatus(CertificateStatus.FAILED);
            certificateRepository.save(certificate);
            throw new RuntimeException("Failed to generate or store certificate", e);
        }
    }

    private String generateFileName(UUID certificateId) {
        //Each tenant gets a new directory of their own
        return String.format("%s/cert-%s.pdf", certificateId, certificateId);
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
