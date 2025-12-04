package com.kulturman.irembotest.domain.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.GONE)
public class CertificateGenerationFailedException extends RuntimeException {
    public CertificateGenerationFailedException(String message) {
        super(message);
    }
}
