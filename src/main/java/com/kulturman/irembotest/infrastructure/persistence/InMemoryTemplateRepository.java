package com.kulturman.irembotest.infrastructure.persistence;

import com.kulturman.irembotest.domain.application.entities.Template;
import com.kulturman.irembotest.domain.ports.TemplateRepository;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Getter
@NoArgsConstructor
public class InMemoryTemplateRepository implements TemplateRepository {
    public final List<Template> savedTemplates = new ArrayList<>();

    public InMemoryTemplateRepository(List<Template> savedTemplates) {
        this.savedTemplates.addAll(savedTemplates);
    }

    @Override
    public void save(Template template) {
        savedTemplates.add(template);
    }

    @Override
    public Optional<Template> findByIdAndTenantId(UUID templateId, UUID tenantId) {
        return savedTemplates.stream().filter(t -> t.getId().equals(templateId) && t.getTenantId().equals(tenantId)).findFirst();
    }

    public void addTemplate(Template template) {
        savedTemplates.add(template);
    }
}
