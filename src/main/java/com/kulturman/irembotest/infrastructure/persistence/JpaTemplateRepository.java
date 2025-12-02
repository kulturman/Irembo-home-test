package com.kulturman.irembotest.infrastructure.persistence;

import com.kulturman.irembotest.domain.entities.Template;
import com.kulturman.irembotest.domain.ports.TemplateRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@AllArgsConstructor
public class JpaTemplateRepository implements TemplateRepository {
    private final TemplateDb templateDb;

    @Override
    public void save(Template template) {
        templateDb.save(template);
    }

    @Override
    public Optional<Template> findByIdAndTenantId(UUID templateId, UUID tenantId) {
        return templateDb.findByIdAndTenantId(templateId, tenantId);
    }

    @Override
    public Page<Template> findByTenantId(UUID tenantId, Pageable pageable) {
        return templateDb.findByTenantId(tenantId, pageable);
    }
}
