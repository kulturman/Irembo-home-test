package com.kulturman.irembotest.infrastructure.persistence;

import com.kulturman.irembotest.domain.entities.Template;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TemplateDb extends JpaRepository<Template, UUID> {
    Page<Template> findByTenantId(UUID tenantId, Pageable pageable);
    Optional<Template> findByIdAndTenantId(UUID id, UUID tenantId);
}
