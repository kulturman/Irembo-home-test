package com.kulturman.irembotest.domain.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class VariableSerializationException extends RuntimeException {
    public VariableSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}