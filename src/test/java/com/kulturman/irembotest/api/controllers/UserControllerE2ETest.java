package com.kulturman.irembotest.api.controllers;

import com.kulturman.irembotest.AbstractIntegrationTest;
import com.kulturman.irembotest.domain.entities.User;
import com.kulturman.irembotest.domain.entities.UserRole;
import com.kulturman.irembotest.domain.ports.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
class UserControllerE2ETest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    private String adminToken;
    private User adminUser;

    @BeforeEach
    void setUp() {
        var adminId = UUID.randomUUID();
        var userId = UUID.randomUUID();

        adminUser = User.builder()
            .id(UUID.randomUUID())
            .tenantId(adminId)
            .email("admin@test.com")
            .password(passwordEncoder.encode("password"))
            .name("Admin User")
            .role(UserRole.ADMIN)
            .build();
        adminUser = userRepository.save(adminUser);
        adminToken = jwtService.generateToken(adminUser);

        userRepository.save(User.builder()
            .id(UUID.randomUUID())
            .tenantId(userId)
            .email("user@test.com")
            .password(passwordEncoder.encode("password"))
            .name("Regular User")
            .role(UserRole.USER)
            .build()
        );
    }

    @Test
    void adminCanCreateUserWithUserRole() throws Exception {
        mockMvc.perform(post("/api/users")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "email": "newuser@test.com",
                        "password": "password123",
                        "name": "New User",
                        "role": "USER"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void adminCanCreateUserWithAdminRole() throws Exception {
        mockMvc.perform(post("/api/users")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "email": "newadmin@test.com",
                        "password": "password123",
                        "name": "New Admin",
                        "role": "ADMIN"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.email").doesNotExist());
    }

    @Test
    void cannotCreateUserWithDuplicateEmail() throws Exception {
        mockMvc.perform(post("/api/users")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "email": "admin@test.com",
                        "password": "password123",
                        "name": "Duplicate Admin",
                        "role": "USER"
                    }
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void adminCanGetAllUsers() throws Exception {
        mockMvc.perform(get("/api/users")
                .header("Authorization", "Bearer " + adminToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").isArray())
            .andExpect(jsonPath("$.content.length()").value(greaterThanOrEqualTo(2)))
            .andExpect(jsonPath("$.content[*].email").value(hasItem("admin@test.com")))
            .andExpect(jsonPath("$.content[*].email").value(hasItem("user@test.com")))
            .andExpect(jsonPath("$.content[*].password").doesNotExist())
            .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(2)))
            .andExpect(jsonPath("$.pageable").exists())
            .andExpect(jsonPath("$.first").value(true))
            .andExpect(jsonPath("$.last").exists());
    }
}
