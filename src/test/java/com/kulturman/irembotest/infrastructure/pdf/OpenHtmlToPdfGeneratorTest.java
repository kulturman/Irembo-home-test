package com.kulturman.irembotest.infrastructure.pdf;

import com.kulturman.irembotest.domain.exceptions.PdfGenerationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class OpenHtmlToPdfGeneratorTest {
    private OpenHtmlToPdfGenerator pdfGenerator;

    @BeforeEach
    void setUp() {
        pdfGenerator = new OpenHtmlToPdfGenerator();
    }

    @Test
    void shouldGeneratePdfWithCertificateTemplate() {
        String html = "<div style='text-align: center;'>" +
                "<h1>Certificate of Achievement</h1>" +
                "<p>This certifies that <strong>John Doe</strong></p>" +
                "<p>has successfully completed the course</p>" +
                "</div>";

        byte[] pdfBytes = pdfGenerator.generatePdf(html);
        assertTrue(pdfBytes.length > 0);
        assertArrayEquals(new byte[]{0x25, 0x50, 0x44, 0x46}, new byte[]{pdfBytes[0], pdfBytes[1], pdfBytes[2], pdfBytes[3]});
    }

    @Test
    void shouldThrowExceptionForInvalidHtml() {
        String invalidHtml = "<div><p>Unclosed tags";

        assertThrows(PdfGenerationException.class, () -> {
            pdfGenerator.generatePdf(invalidHtml);
        });
    }
}
