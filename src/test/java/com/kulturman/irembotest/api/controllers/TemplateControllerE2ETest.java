package com.kulturman.irembotest.api.controllers;

import com.kulturman.irembotest.AbstractIntegrationTest;
import com.kulturman.irembotest.domain.entities.Template;
import com.kulturman.irembotest.domain.entities.User;
import com.kulturman.irembotest.infrastructure.persistence.TemplateDb;
import com.kulturman.irembotest.infrastructure.persistence.UserDb;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TemplateControllerE2ETest extends AbstractIntegrationTest {
    @Autowired
    UserDb userRepository;

    @Autowired
    TemplateDb templateRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    private UUID tenantA;
    private UUID tenantB;
    private User userA;
    private User userB;
    private String tokenA;
    private String tokenB;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        templateRepository.deleteAll();

        tenantA = UUID.randomUUID();
        tenantB = UUID.randomUUID();

        userA = User.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantA)
                .email("userA@example.com")
                .password(passwordEncoder.encode("password"))
                .name("User A")
                .build();

        userB = User.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantB)
                .email("userB@example.com")
                .password(passwordEncoder.encode("password"))
                .name("User B")
                .build();

        userRepository.save(userA);
        userRepository.save(userB);

        tokenA = jwtService.generateToken(userA);
        tokenB = jwtService.generateToken(userB);

        Template templateA1 = Template.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantA)
                .name("Template A1")
                .content("Content for tenant A template 1")
                .variables("[\"var1\"]")
                .build();

        Template templateA2 = Template.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantA)
                .name("Template A2")
                .content("Content for tenant A template 2")
                .variables("[\"var2\"]")
                .build();

        Template templateB1 = Template.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantB)
                .name("Template B1")
                .content("Content for tenant B template 1")
                .variables("[\"var3\"]")
                .build();

        templateRepository.save(templateA1);
        templateRepository.save(templateA2);
        templateRepository.save(templateB1);
    }

    @Test
    void shouldReturnOnlyTenantATemplatesForUserA() throws Exception {
        mockMvc.perform(get("/api/templates")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[?(@.name == 'Template A1')]").exists())
                .andExpect(jsonPath("$.content[?(@.name == 'Template A2')]").exists())
                .andExpect(jsonPath("$.content[?(@.name == 'Template B1')]").doesNotExist())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void shouldReturnOnlyTenantBTemplatesForUserB() throws Exception {
        mockMvc.perform(get("/api/templates")
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[?(@.name == 'Template B1')]").exists())
                .andExpect(jsonPath("$.content[?(@.name == 'Template A1')]").doesNotExist())
                .andExpect(jsonPath("$.content[?(@.name == 'Template A2')]").doesNotExist())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void shouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/templates")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldSupportPagination() throws Exception {
        mockMvc.perform(get("/api/templates")
                        .header("Authorization", "Bearer " + tokenA)
                        .param("page", "0")
                        .param("size", "1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    void shouldSupportSorting() throws Exception {
        mockMvc.perform(get("/api/templates")
                        .header("Authorization", "Bearer " + tokenA)
                        .param("sort", "name,desc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Template A2"))
                .andExpect(jsonPath("$.content[1].name").value("Template A1"));
    }

    @Test
    void shouldCreateTemplateSuccessfully() throws Exception {
        String requestBody = """
                {
                    "name": "New Certificate Template",
                    "content": "<html><body>Certificate for {name} - Course: {course} - Date: {date}</body></html>"
                }
                """;

        String response = mockMvc.perform(
            post("/api/templates")
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody)
            )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.id").isNotEmpty())
            .andReturn()
            .getResponse()
            .getContentAsString();

        UUID createdTemplateId = UUID.fromString(objectMapper.readTree(response).get("id").asText());

        Template createdTemplate = templateRepository.findById(createdTemplateId).orElseThrow();

        assertEquals("New Certificate Template", createdTemplate.getName());
        assertEquals("<html><body>Certificate for {name} - Course: {course} - Date: {date}</body></html>", createdTemplate.getContent());
        assertEquals("[\"name\", \"course\", \"date\"]", createdTemplate.getVariables());
        assertEquals(tenantA, createdTemplate.getTenantId());
        assertNotNull(createdTemplate.getCreatedAt());
        assertNotNull(createdTemplate.getUpdatedAt());
    }

    @Test
    void shouldUpdateTemplateSuccessfully() throws Exception {
        Template existingTemplate = templateRepository.findByTenantId(tenantA, Pageable.unpaged())
                .getContent().stream()
                .filter(t -> t.getName().equals("Template A1"))
                .findFirst()
                .orElseThrow();

        String updateRequestBody = """
                {
                    "name": "Updated Template Name",
                    "content": "<html><body>Updated content with {newVar1} and {newVar2}</body></html>"
                }
                """;

        mockMvc.perform(
            put("/api/templates/" + existingTemplate.getId())
                .header("Authorization", "Bearer " + tokenA)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequestBody)
            )
            .andExpect(status().isNoContent());

        Template updatedTemplate = templateRepository.findById(existingTemplate.getId()).orElseThrow();

        assertEquals("Updated Template Name", updatedTemplate.getName());
        assertEquals("<html><body>Updated content with {newVar1} and {newVar2}</body></html>", updatedTemplate.getContent());
        assertEquals("[\"newVar1\", \"newVar2\"]", updatedTemplate.getVariables());
        assertEquals(tenantA, updatedTemplate.getTenantId());
        assertEquals(existingTemplate.getCreatedAt(), updatedTemplate.getCreatedAt());
        assertNotEquals(existingTemplate.getUpdatedAt(), updatedTemplate.getUpdatedAt());
    }

    @Test
    void shouldGetSingleTemplateSuccessfully() throws Exception {
        Template template = Template.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantA)
                .name("Single Template Test")
                .content("<html><body>Test content for {userName}</body></html>")
                .variables("[\"userName\"]")
                .build();
        templateRepository.save(template);

        mockMvc.perform(get("/api/templates/{id}", template.getId())
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(template.getId().toString()))
                .andExpect(jsonPath("$.name").value("Single Template Test"))
                .andExpect(jsonPath("$.content").value("<html><body>Test content for {userName}</body></html>"))
                .andExpect(jsonPath("$.variables").value("[\"userName\"]"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }
}
