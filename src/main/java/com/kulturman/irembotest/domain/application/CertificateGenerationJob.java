package com.kulturman.irembotest.domain.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kulturman.irembotest.domain.application.entities.Certificate;
import com.kulturman.irembotest.domain.entities.CertificateStatus;
import com.kulturman.irembotest.domain.ports.CertificateRepository;
import com.kulturman.irembotest.domain.ports.PdfGenerator;
import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public class CertificateGenerationJob {
    private final CertificateRepository certificateRepository;
    private final PdfGenerator pdfGenerator;

    public void execute(UUID certificateId) {
        var certificate = certificateRepository.findById(certificateId).orElseThrow(() -> new RuntimeException("Certificate not found"));
        certificate.setStatus(CertificateStatus.PROCESSING);
        certificateRepository.save(certificate);

        var processedTemplate = replaceVariables(certificate);
        var generatedPdf = pdfGenerator.generatePdf(processedTemplate);
        certificate.setStatus(CertificateStatus.COMPLETED);
        certificateRepository.save(certificate);
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
