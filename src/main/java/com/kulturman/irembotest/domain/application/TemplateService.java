package com.kulturman.irembotest.domain.application;

import com.kulturman.irembotest.domain.application.entities.Template;
import com.kulturman.irembotest.domain.ports.TenancyProvider;
import com.kulturman.irembotest.domain.ports.TemplateRepository;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TemplateService {
    private final TenancyProvider tenancyProvider;
    private final TemplateRepository templateRepository;

    public Template createTemplate(CreateTemplateRequest createTemplateRequest) {
        var tenantId = tenancyProvider.getCurrentTenantId();
        var template = Template.builder()
            .name(createTemplateRequest.getName())
            .content(createTemplateRequest.getContent())
            .tenantId(tenantId).build();

        templateRepository.save(template);
        return template;
    }
}
