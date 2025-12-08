package com.kulturman.irembotest.infrastructure.configuration;

import com.kulturman.irembotest.domain.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TemplateNotFoundException.class)
    public ProblemDetail handleTemplateNotFoundException(TemplateNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Template Not Found");
        return problemDetail;
    }

    @ExceptionHandler(CertificateNotFoundException.class)
    public ProblemDetail handleCertificateNotFoundException(CertificateNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Certificate Not Found");
        return problemDetail;
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ProblemDetail handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("User Already Exists");
        return problemDetail;
    }

    @ExceptionHandler(CertificateNotReadyException.class)
    public ProblemDetail handleCertificateNotReadyException(CertificateNotReadyException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.ACCEPTED, ex.getMessage());
        problemDetail.setTitle("Certificate Not Ready");
        return problemDetail;
    }

    @ExceptionHandler(CertificateFileNotFoundException.class)
    public ProblemDetail handleCertificateFileNotFoundException(CertificateFileNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Certificate File Not Found");
        return problemDetail;
    }

    @ExceptionHandler(CertificateGenerationFailedException.class)
    public ProblemDetail handleCertificateGenerationFailedException(CertificateGenerationFailedException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.GONE, ex.getMessage());
        problemDetail.setTitle("Certificate Generation Failed");
        return problemDetail;
    }

    @ExceptionHandler(PdfGenerationException.class)
    public ProblemDetail handlePdfGenerationException(PdfGenerationException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        problemDetail.setTitle("PDF Generation Failed");
        return problemDetail;
    }

    @ExceptionHandler(VariableSerializationException.class)
    public ProblemDetail handleVariableSerializationException(VariableSerializationException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        problemDetail.setTitle("Variable Serialization Failed");
        return problemDetail;
    }

    @ExceptionHandler(VariableProcessingException.class)
    public ProblemDetail handleVariableProcessingException(VariableProcessingException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        problemDetail.setTitle("Variable Processing Failed");
        return problemDetail;
    }

    @ExceptionHandler(QrCodeGenerationException.class)
    public ProblemDetail handleQrCodeGenerationException(QrCodeGenerationException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        problemDetail.setTitle("QR Code Generation Failed");
        return problemDetail;
    }

    @ExceptionHandler(FileStorageException.class)
    public ProblemDetail handleFileStorageException(FileStorageException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        problemDetail.setTitle("File Storage Failed");
        return problemDetail;
    }
}
