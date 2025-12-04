package com.kulturman.irembotest.domain.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.ACCEPTED)
public class CertificateNotReadyException extends RuntimeException {
    public CertificateNotReadyException(String message) {
        super(message);
    }
}
