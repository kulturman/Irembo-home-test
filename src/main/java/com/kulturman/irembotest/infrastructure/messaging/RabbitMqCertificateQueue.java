package com.kulturman.irembotest.infrastructure.messaging;

import com.kulturman.irembotest.domain.entities.Certificate;
import com.kulturman.irembotest.domain.ports.CertificateQueue;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class RabbitMqCertificateQueue implements CertificateQueue {
    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publish(Certificate certificate) {
        log.info("Publishing certificate generation job to RabbitMQ. Certificate ID: {}, Tenant ID: {}, Template ID: {}, Status: {}",
                certificate.getId(),
                certificate.getTenantId(),
                certificate.getTemplate().getId(),
                certificate.getStatus());

        rabbitTemplate.convertAndSend("certificate.generation.queue", certificate.getId().toString());

        log.info("Certificate generation job published successfully for Certificate ID: {}", certificate.getId());
    }
}
