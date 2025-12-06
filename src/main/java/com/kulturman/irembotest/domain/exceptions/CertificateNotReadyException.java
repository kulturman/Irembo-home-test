package com.kulturman.irembotest.domain.exceptions;

public class CertificateNotReadyException extends RuntimeException {
    public CertificateNotReadyException(String message) {
        super(message);
    }
}
