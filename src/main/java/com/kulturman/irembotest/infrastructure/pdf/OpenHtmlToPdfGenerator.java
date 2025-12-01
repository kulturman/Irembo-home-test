package com.kulturman.irembotest.infrastructure.pdf;

import com.kulturman.irembotest.domain.exceptions.PdfGenerationException;
import com.kulturman.irembotest.domain.ports.PdfGenerator;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;

@Component
public class OpenHtmlToPdfGenerator implements PdfGenerator {
    @Override
    public byte[] generatePdf(String html) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            builder.run();

            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new PdfGenerationException("Failed to generate PDF", e);
        }
    }
}
