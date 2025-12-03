package com.kulturman.irembotest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kulturman.irembotest.infrastructure.configuration.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.RabbitMQContainer;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public abstract class AbstractIntegrationTest {
    @Autowired
    public JwtService jwtService;

    @Autowired
    public MockMvc mockMvc;

    public ObjectMapper objectMapper = new ObjectMapper();

    @ServiceConnection
    public static PostgreSQLContainer<?> postgresContainer = TestContainersConfig.getPostgresContainer();

    @ServiceConnection
    public static RabbitMQContainer rabbitMQContainer = TestContainersConfig.getRabbitMqContainer();
}
