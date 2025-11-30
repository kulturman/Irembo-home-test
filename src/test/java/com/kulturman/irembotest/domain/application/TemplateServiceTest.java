package com.kulturman.irembotest.domain.application;

import com.kulturman.irembotest.domain.application.entities.Template;
import com.kulturman.irembotest.domain.exceptions.TemplateNotFoundException;
import com.kulturman.irembotest.domain.ports.TenancyProvider;
import com.kulturman.irembotest.infrastructure.persistence.InMemoryTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TemplateServiceTest {
    @Mock
    TenancyProvider tenancyProvider;

    InMemoryTemplateRepository templateRepository;
    TemplateService templateService;
    UUID tenantId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        templateRepository = new InMemoryTemplateRepository();
        templateService = new TemplateService(tenancyProvider, templateRepository);
        when(tenancyProvider.getCurrentTenantId()).thenReturn(tenantId);
    }

    @Test
    void shouldCreateTemplateWithCurrentTenantId() {
        var createTemplateRequest = CreateTemplateRequest
            .builder().name("name").content("This is my first template ever").build();

        templateService.createTemplate(createTemplateRequest);

        assertEquals(1, templateRepository.getSavedTemplates().size());
        Template savedTemplate = templateRepository.getSavedTemplates().getFirst();
        assertEquals(tenantId, savedTemplate.getTenantId());
        assertNotNull(savedTemplate.getId());
        assertEquals(createTemplateRequest.getName(), savedTemplate.getName());
        assertEquals(createTemplateRequest.getContent(), savedTemplate.getContent());
    }

    @Test
    void shouldExtractVariablesFromTemplate() {
        var createTemplateRequest = CreateTemplateRequest
            .builder().name("name").content("Hello guys my name is {name}, {lastname}").build();
        var template = templateService.createTemplate(createTemplateRequest);

        assertNotNull(template.getVariables());
        assertEquals("[\"name\", \"lastname\"]", template.getVariables());
    }

    @Test
    void shouldUpdateVariablesOnUpdate() {
        var templateToUpdateId = UUID.randomUUID();

        templateRepository.addTemplate(
            Template.builder().id(templateToUpdateId).tenantId(tenantId).name("name").content("Hello guys my name is {name}").variables("[\"name\"]").build()
        );

        UpdateTemplateRequest updateTemplateRequest = UpdateTemplateRequest
            .builder().name("new name").content("This is {name} and I am {age}").build();

        templateService.updateTemplate(templateToUpdateId, updateTemplateRequest);
        var updatedTemplate = templateRepository.getSavedTemplates().getFirst();

        assertEquals("[\"name\", \"age\"]", updatedTemplate.getVariables());
    }

    @Test
    void shouldFailIfTemplateDoesNotBelongToCurrentTenant() {
        var templateToUpdateId = UUID.randomUUID();
        var differentTenantId = UUID.randomUUID();

        templateRepository.addTemplate(
            Template.builder()
                .id(templateToUpdateId)
                .tenantId(tenantId)
                .name("name")
                .content("Hello {name}")
                .variables("[\"name\"]")
                .build()
        );

        // Try to update with a different tenant ID
        when(tenancyProvider.getCurrentTenantId()).thenReturn(differentTenantId);

        UpdateTemplateRequest updateRequest = UpdateTemplateRequest.builder()
            .name("new name")
            .content("Updated content")
            .build();

        assertThrows(TemplateNotFoundException.class, () -> templateService.updateTemplate(templateToUpdateId, updateRequest));
    }

}
