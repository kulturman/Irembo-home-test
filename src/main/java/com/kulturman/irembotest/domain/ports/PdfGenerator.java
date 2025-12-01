package com.kulturman.irembotest.domain.ports;

import com.kulturman.irembotest.domain.exceptions.PdfGenerationException;

public interface PdfGenerator {
    byte[] generatePdf(String html) throws PdfGenerationException;
}
