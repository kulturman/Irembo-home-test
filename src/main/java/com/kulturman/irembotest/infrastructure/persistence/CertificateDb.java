package com.kulturman.irembotest.infrastructure.persistence;

import com.kulturman.irembotest.domain.entities.Certificate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CertificateDb extends JpaRepository<Certificate, UUID> {
    Optional<Certificate> findByIdAndTenantId(UUID id, UUID tenantId);
    Optional<Certificate> findByDownloadToken(String downloadToken);
    Page<Certificate> findByTemplateIdAndTenantId(UUID templateId, UUID tenantId, Pageable pageable);
}
