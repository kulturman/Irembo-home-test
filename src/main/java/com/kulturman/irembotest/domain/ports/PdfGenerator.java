package com.kulturman.irembotest.domain.ports;

public interface PdfGenerator {
    byte[] generatePdf(String html);
}
