package com.kulturman.irembotest.domain.exceptions;

public class CertificateFileNotFoundException extends RuntimeException {
    public CertificateFileNotFoundException(String message) {
        super(message);
    }

    public CertificateFileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
