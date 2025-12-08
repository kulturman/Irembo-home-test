package com.kulturman.irembotest.domain.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class VariableProcessingException extends RuntimeException {
    public VariableProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
