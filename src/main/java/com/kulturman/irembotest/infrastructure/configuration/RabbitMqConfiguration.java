package com.kulturman.irembotest.infrastructure.configuration;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMqConfiguration {

    @Bean
    public Queue certificateGenerationQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", "");
        args.put("x-dead-letter-routing-key", "certificate.generation.dlq");
        return new Queue("certificate.generation.queue", true, false, false, args);
    }

    @Bean
    public Queue certificateGenerationDlq() {
        return new Queue("certificate.generation.dlq", true);
    }
}
