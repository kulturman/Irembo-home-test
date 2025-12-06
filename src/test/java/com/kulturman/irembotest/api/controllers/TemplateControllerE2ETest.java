package com.kulturman.irembotest.api.controllers;

import com.kulturman.irembotest.AbstractIntegrationTest;
import com.kulturman.irembotest.domain.entities.Template;
import com.kulturman.irembotest.domain.entities.User;
import com.kulturman.irembotest.fixtures.FixtureUtils;
import com.kulturman.irembotest.infrastructure.persistence.TemplateDb;
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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestExecutionListeners({
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class,
    DbUnitTestExecutionListener.class
})
@DatabaseSetup({"/fixtures/users.xml", "/fixtures/certificate-test-data.xml"})
public class TemplateControllerE2ETest extends AbstractIntegrationTest {

    @Autowired
    private UserDb userRepository;

    @Autowired
    private TemplateDb templateRepository;

    @Test
    void shouldReturnOnlyTenantATemplatesForUserA() throws Exception {
        User userA = userRepository.findById(FixtureUtils.USER_A_ID).orElseThrow();
        String tokenA = jwtService.generateToken(userA);

        mockMvc.perform(
            get("/api/templates")
            .header("Authorization", "Bearer " + tokenA)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk()
        )
        .andExpect(jsonPath("$.content").isArray())
        .andExpect(jsonPath("$.content.length()").value(5))
        .andExpect(jsonPath("$.content[?(@.name == 'Certificate of Achievement A')]").exists())
        .andExpect(jsonPath("$.content[?(@.name == 'Template A1')]").exists())
        .andExpect(jsonPath("$.content[?(@.name == 'Template A2')]").exists())
        .andExpect(jsonPath("$.content[?(@.name == 'Template B1')]").doesNotExist())
        .andExpect(jsonPath("$.totalElements").value(5));
    }

    @Test
    void shouldReturnOnlyTenantBTemplatesForUserB() throws Exception {
        User userB = userRepository.findById(FixtureUtils.USER_B_ID).orElseThrow();
        String tokenB = jwtService.generateToken(userB);

        mockMvc.perform(
            get("/api/templates")
            .header("Authorization", "Bearer " + tokenB)
            .contentType(MediaType.APPLICATION_JSON)
        )
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
        mockMvc.perform(
            get("/api/templates")
            .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isForbidden());
    }

    @Test
    void shouldCreateTemplateSuccessfully() throws Exception {
        User userA = userRepository.findById(FixtureUtils.USER_A_ID).orElseThrow();
        String tokenA = jwtService.generateToken(userA);

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
        assertEquals(FixtureUtils.TENANT_A_ID, createdTemplate.getTenantId());
        assertNotNull(createdTemplate.getCreatedAt());
        assertNotNull(createdTemplate.getUpdatedAt());
    }

    @Test
    void shouldUpdateTemplateSuccessfully() throws Exception {
        User userA = userRepository.findById(FixtureUtils.USER_A_ID).orElseThrow();
        String tokenA = jwtService.generateToken(userA);

        Template existingTemplate = templateRepository.findById(FixtureUtils.TEMPLATE_A1_ID).orElseThrow();

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
        assertEquals(FixtureUtils.TENANT_A_ID, updatedTemplate.getTenantId());
        assertEquals(existingTemplate.getCreatedAt(), updatedTemplate.getCreatedAt());
        assertNotEquals(existingTemplate.getUpdatedAt(), updatedTemplate.getUpdatedAt());
    }

    @Test
    void shouldGetSingleTemplateSuccessfully() throws Exception {
        User userA = userRepository.findById(FixtureUtils.USER_A_ID).orElseThrow();
        String tokenA = jwtService.generateToken(userA);

        mockMvc.perform(
            get("/api/templates/{id}", FixtureUtils.TEMPLATE_A1_ID)
            .header("Authorization", "Bearer " + tokenA)
            .contentType(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(FixtureUtils.TEMPLATE_A1_ID.toString()))
        .andExpect(jsonPath("$.name").value("Template A1"))
        .andExpect(jsonPath("$.content").value("Content for tenant A template 1"))
        .andExpect(jsonPath("$.variables").value("[\"var1\"]"))
        .andExpect(jsonPath("$.createdAt").exists())
        .andExpect(jsonPath("$.updatedAt").exists());
    }
}
