package com.kulturman.irembotest.domain.application;

import com.kulturman.irembotest.domain.application.entities.Template;
import com.kulturman.irembotest.domain.exceptions.TemplateNotFoundException;
import com.kulturman.irembotest.domain.ports.TenancyProvider;
import com.kulturman.irembotest.domain.ports.TemplateRepository;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AllArgsConstructor
public class TemplateService {
    private final TenancyProvider tenancyProvider;
    private final TemplateRepository templateRepository;
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\s*(\\w+)\\s*\\}");

    public Template createTemplate(CreateTemplateRequest createTemplateRequest) {
        var tenantId = tenancyProvider.getCurrentTenantId();
        var variables = extractVariables(createTemplateRequest.getContent());

        var template = Template.builder()
            .id(UUID.randomUUID())
            .name(createTemplateRequest.getName())
            .content(createTemplateRequest.getContent())
            .variables(variables)
            .tenantId(tenantId).build();

        templateRepository.save(template);
        return template;
    }

    private String extractVariables(String content) {
        List<String> variables = new ArrayList<>();
        Matcher matcher = VARIABLE_PATTERN.matcher(content);

        while (matcher.find()) {
            String variable = matcher.group(1);
            if (!variables.contains(variable)) {
                variables.add(variable);
            }
        }

        return "[" + String.join(", ", variables.stream()
            .map(v -> "\"" + v + "\"")
            .toList()) + "]";
    }

    public void updateTemplate(UUID templateToUpdateId, UpdateTemplateRequest updateTemplateRequest) {
        var tenantId = tenancyProvider.getCurrentTenantId();
        var template = templateRepository.findByIdAndTenantId(templateToUpdateId, tenantId)
            .orElseThrow(() -> new TemplateNotFoundException("Template not found"));

        template.setContent(updateTemplateRequest.getContent());
        template.setName(updateTemplateRequest.getName());
        template.setVariables(extractVariables(updateTemplateRequest.getContent()));
        templateRepository.save(template);
    }
}
