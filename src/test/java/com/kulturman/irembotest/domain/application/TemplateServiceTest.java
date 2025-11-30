package com.kulturman.irembotest.domain.application;

import com.kulturman.irembotest.domain.application.entities.Template;
import com.kulturman.irembotest.domain.ports.TenancyProvider;
import com.kulturman.irembotest.infrastructure.persistence.InMemoryTemplateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.Collections;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TemplateServiceTest {
    @Mock
    TenancyProvider tenancyProvider;

    @Test
    void shouldCreateTemplateWithCurrentTenantId() {
        InMemoryTemplateRepository templateRepository = new InMemoryTemplateRepository();
        var templateService = new TemplateService(tenancyProvider, templateRepository);
        var createTemplateRequest = CreateTemplateRequest
            .builder().name("name").content("This is my first template ever").build();
        UUID tenantId = UUID.randomUUID();
        when(tenancyProvider.getCurrentTenantId()).thenReturn(tenantId);

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
        InMemoryTemplateRepository templateRepository = new InMemoryTemplateRepository();
        var templateService = new TemplateService(tenancyProvider, templateRepository);

        var createTemplateRequest = CreateTemplateRequest
            .builder().name("name").content("Hello guys my name is {{name}}, {{lastname}}").build();
        var template = templateService.createTemplate(createTemplateRequest);

        assertNotNull(template.getVariables());
        assertEquals("[\"name\", \"lastname\"]", template.getVariables());
    }

    @Test
    void shouldUpdateVariablesOnUpdate() {
        var templateToUpdateId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        when(tenancyProvider.getCurrentTenantId()).thenReturn(tenantId);

        InMemoryTemplateRepository templateRepository = new InMemoryTemplateRepository(
            Collections.singletonList(Template.builder().id(templateToUpdateId).tenantId(tenantId).name("name").content("Hello guys my name is {{name}}").variables("[\"name\"]").build())
        );

        var templateService = new TemplateService(tenancyProvider, templateRepository);
        UpdateTemplateRequest updateTemplateRequest = UpdateTemplateRequest
            .builder().name("new name").content("This is {{name}} and I am {{age}}").build();

        templateService.updateTemplate(templateToUpdateId, updateTemplateRequest);
        var updatedTemplate = templateRepository.getSavedTemplates().getFirst();

        assertEquals("[\"name\", \"age\"]", updatedTemplate.getVariables());
    }

}
