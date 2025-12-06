package com.kulturman.irembotest.api.controllers;

import com.kulturman.irembotest.AbstractIntegrationTest;
import com.kulturman.irembotest.domain.entities.User;
import com.kulturman.irembotest.fixtures.FixtureUtils;
import com.kulturman.irembotest.infrastructure.persistence.UserDb;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DatabaseSetup;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestExecutionListeners({
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class
})
@DatabaseSetup({"/fixtures/users.xml", "/fixtures/certificate-test-data.xml"})
class UserControllerE2ETest extends AbstractIntegrationTest {

    @Autowired
    private UserDb userRepository;

    @Test
    void adminCanCreateUserWithUserRole() throws Exception {
        User adminUser = userRepository.findById(FixtureUtils.ADMIN_USER_ID).orElseThrow();
        String adminToken = jwtService.generateToken(adminUser);

        mockMvc.perform(
            post("/api/users")
            .header("Authorization", "Bearer " + adminToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "email": "newuser@test.com",
                    "password": "password123",
                    "name": "New User",
                    "role": "USER"
                }
                """
            )
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void adminCanCreateUserWithAdminRole() throws Exception {
        User adminUser = userRepository.findById(FixtureUtils.ADMIN_USER_ID).orElseThrow();
        String adminToken = jwtService.generateToken(adminUser);

        mockMvc.perform(
            post("/api/users")
            .header("Authorization", "Bearer " + adminToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "email": "newadmin@test.com",
                    "password": "password123",
                    "name": "New Admin",
                    "role": "ADMIN"
                }
                """
            )
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").exists())
        .andExpect(jsonPath("$.email").doesNotExist());
    }

    @Test
    void cannotCreateUserWithDuplicateEmail() throws Exception {
        User adminUser = userRepository.findById(FixtureUtils.ADMIN_USER_ID).orElseThrow();
        String adminToken = jwtService.generateToken(adminUser);

        mockMvc.perform(
            post("/api/users")
            .header("Authorization", "Bearer " + adminToken)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                    "email": "admin@test.com",
                    "password": "password123",
                    "name": "Duplicate Admin",
                    "role": "USER"
                }
                """
            )
        )
        .andExpect(status().isBadRequest());
    }

    @Test
    void adminCanGetAllUsers() throws Exception {
        User adminUser = userRepository.findById(FixtureUtils.ADMIN_USER_ID).orElseThrow();
        String adminToken = jwtService.generateToken(adminUser);

        mockMvc.perform(
            get("/api/users")
            .header("Authorization", "Bearer " + adminToken)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(greaterThanOrEqualTo(4)))
        .andExpect(jsonPath("$.content[*].email").value(hasItem("admin@test.com")))
        .andExpect(jsonPath("$.content[*].email").value(hasItem("user@test.com")))
        .andExpect(jsonPath("$.content[*].password").doesNotExist())
        .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(4)))
        .andExpect(jsonPath("$.pageable").exists())
        .andExpect(jsonPath("$.first").value(true))
        .andExpect(jsonPath("$.last").exists());
    }
}
