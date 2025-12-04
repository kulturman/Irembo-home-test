package com.kulturman.irembotest.domain.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class CertificateFileNotFoundException extends RuntimeException {
    public CertificateFileNotFoundException(String message) {
        super(message);
    }

    public CertificateFileNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
