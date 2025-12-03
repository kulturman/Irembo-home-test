package com.kulturman.irembotest.infrastructure.messaging;

import com.kulturman.irembotest.domain.application.CertificateGenerationJob;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@AllArgsConstructor
@Slf4j
public class CertificateGenerationListener {
    private final CertificateGenerationJob certificateGenerationJob;

    @RabbitListener(queues = "certificate.generation.queue")
    public void processCertificateGeneration(String certificateId) {
        try {
            log.info("Received certificate generation request for Certificate ID: {}", certificateId);

            UUID id = UUID.fromString(certificateId);
            certificateGenerationJob.execute(id);

            log.info("Successfully processed certificate generation for Certificate ID: {}", certificateId);
        } catch (IllegalArgumentException e) {
            log.error("Invalid certificate ID format: {}", certificateId, e);
            throw e;
        } catch (Exception e) {
            log.error("Failed to process certificate generation for Certificate ID: {}", certificateId, e);
            throw e;
        }
    }
}
