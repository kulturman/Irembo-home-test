package com.kulturman.irembotest.domain.ports;

import com.kulturman.irembotest.domain.application.entities.Template;

import java.util.Optional;
import java.util.UUID;

public interface TemplateRepository {
    void save(Template template);
    Optional<Template> findByIdAndTenantId(UUID templateId, UUID tenantId);
}
