package com.kulturman.irembotest.domain.exceptions;

public class CertificateGenerationFailedException extends RuntimeException {
    public CertificateGenerationFailedException(String message) {
        super(message);
    }
}
