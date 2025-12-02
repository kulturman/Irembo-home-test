package com.kulturman.irembotest.infrastructure.persistence;

import com.kulturman.irembotest.domain.entities.Template;
import com.kulturman.irembotest.domain.ports.TemplateRepository;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @Override
    public Page<Template> findByTenantId(UUID tenantId, Pageable pageable) {
        List<Template> filtered = savedTemplates.stream()
                .filter(t -> t.getTenantId().equals(tenantId))
                .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());

        List<Template> pageContent = start < filtered.size()
                ? filtered.subList(start, end)
                : new ArrayList<>();

        return new PageImpl<>(pageContent, pageable, filtered.size());
    }

    public void addTemplate(Template template) {
        savedTemplates.add(template);
    }
}
