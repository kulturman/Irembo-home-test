package com.kulturman.irembotest.domain.ports;

import com.kulturman.irembotest.domain.entities.Template;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface TemplateRepository {
    void save(Template template);
    Optional<Template> findByIdAndTenantId(UUID templateId, UUID tenantId);
    Page<Template> findByTenantId(UUID tenantId, Pageable pageable);
}
