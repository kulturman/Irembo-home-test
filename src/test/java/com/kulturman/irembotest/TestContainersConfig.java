package com.kulturman.irembotest;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.utility.DockerImageName;

public class TestContainersConfig {

    private static final PostgreSQLContainer<?> POSTGRES_CONTAINER;
    private static final RabbitMQContainer RABBITMQ_CONTAINER;

    static {
        POSTGRES_CONTAINER = new PostgreSQLContainer<>(DockerImageName.parse("postgres:17-alpine"))
                .withDatabaseName("irembo_test")
                .withUsername("test")
                .withPassword("test")
                .withReuse(true);

        RABBITMQ_CONTAINER = new RabbitMQContainer(DockerImageName.parse("rabbitmq:4.0-alpine"))
                .withReuse(true);

        POSTGRES_CONTAINER.start();
        RABBITMQ_CONTAINER.start();
    }

    public static PostgreSQLContainer<?> getPostgresContainer() {
        return POSTGRES_CONTAINER;
    }

    public static RabbitMQContainer getRabbitMqContainer() {
        return RABBITMQ_CONTAINER;
    }
}
