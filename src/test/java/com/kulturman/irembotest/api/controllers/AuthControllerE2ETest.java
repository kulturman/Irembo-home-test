package com.kulturman.irembotest.api.controllers;

import com.kulturman.irembotest.AbstractIntegrationTest;
import com.kulturman.irembotest.api.dto.LoginRequest;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestExecutionListeners({
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class
})
@DatabaseSetup("/fixtures/auth-test-data.xml")
public class AuthControllerE2ETest extends AbstractIntegrationTest {
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password123";

    @Test
    void shouldLoginSuccessfully() throws Exception {
        LoginRequest request = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").exists())
            .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void shouldFailLoginWithInvalidPassword() throws Exception {
        LoginRequest request = new LoginRequest(TEST_EMAIL, "wrongpassword");

        mockMvc.perform(
            post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void shouldFailLoginWithInvalidEmail() throws Exception {
        LoginRequest request = new LoginRequest("TEST_EMAIL", TEST_PASSWORD);

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
            )
            .andExpect(status().is4xxClientError());
    }
}
