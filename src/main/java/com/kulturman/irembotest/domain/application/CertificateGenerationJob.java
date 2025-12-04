package com.kulturman.irembotest.domain.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import com.kulturman.irembotest.domain.entities.Certificate;
import com.kulturman.irembotest.domain.entities.CertificateStatus;
import com.kulturman.irembotest.domain.ports.CertificateRepository;
import com.kulturman.irembotest.domain.ports.FileStorage;
import com.kulturman.irembotest.domain.ports.PdfGenerator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Transactional
public class CertificateGenerationJob {
    private final CertificateRepository certificateRepository;
    private final PdfGenerator pdfGenerator;
    private final FileStorage fileStorage;

    @Value("${app.back-url}")
    private String baseUrl;

    public void execute(UUID certificateId) {
        var certificate = certificateRepository.findById(certificateId).orElseThrow(() -> new RuntimeException("Certificate not found"));

        try {
            certificate.setStatus(CertificateStatus.PROCESSING);
            certificateRepository.save(certificate);

            var processedTemplate = replaceVariables(certificate);
            var pdfContent = addQrCode(pdfGenerator.generatePdf(processedTemplate), certificateId);
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

    private byte[] addQrCode(byte[] pdfContent, UUID certificateId) throws IOException {
        try (
            PDDocument document = PDDocument.load(pdfContent);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        ) {
            String qrContent = generateVerifyUrl(certificateId);
            BufferedImage qrImage = generateQRCodeImage(qrContent);

            // Get the last page to add QR code
            PDPage lastPage = document.getPage(document.getNumberOfPages() - 1);

            // Convert BufferedImage to PDImageXObject
            PDImageXObject pdImage = LosslessFactory.createFromImage(document, qrImage);

            // Add QR code to page (bottom right corner)
            try (PDPageContentStream contentStream = new PDPageContentStream(
                document, lastPage, PDPageContentStream.AppendMode.APPEND, true)) {

                float xPosition = lastPage.getMediaBox().getWidth() - 120; // 20px margin from right
                float yPosition = 20; // 20px from bottom

                contentStream.drawImage(pdImage, xPosition, yPosition, 100, 100);
            }

            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (WriterException e) {
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }

    private BufferedImage generateQRCodeImage(String content) throws WriterException {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        var bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, 100, 100);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    private String generateVerifyUrl(UUID certificateId) {
        return String.format("%s/api/public/verify-certificate/%s", baseUrl, certificateId.toString());
    }

    private String generateFileName(UUID certificateId) {
        //Each tenant gets a new directory of their own
        return String.format("%s/cert-%s.pdf", certificateId, certificateId);
    }

    private String replaceVariables(Certificate certificate) {
        var templateContent = certificate.getTemplate().getContent();
        try {
            var variables = certificate.getVariablesAsList();

            for (Variable variable : variables) {
                templateContent = templateContent.replace("{" + variable.getKey() + "}", variable.getValue());
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return templateContent;
    }
}
