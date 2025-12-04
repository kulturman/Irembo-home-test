package com.kulturman.irembotest.infrastructure.persistence;

import com.kulturman.irembotest.domain.entities.Certificate;
import com.kulturman.irembotest.domain.ports.CertificateRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@AllArgsConstructor
public class JpaCertificateRepository implements CertificateRepository {
    private final CertificateDb certificateDb;

    @Override
    public void save(Certificate certificate) {
        certificateDb.save(certificate);
    }

    @Override
    public Optional<Certificate> findById(UUID certificateId) {
        return certificateDb.findById(certificateId);
    }

    @Override
    public Optional<Certificate> findByIdAndTenantId(UUID certificateId, UUID tenantId) {
        return certificateDb.findByIdAndTenantId(certificateId, tenantId);
    }

    @Override
    public Optional<Certificate> findByDownloadToken(String downloadToken) {
        return certificateDb.findByDownloadToken(downloadToken);
    }

    @Override
    public Page<Certificate> findByTemplateIdAndTenantId(UUID templateId, UUID tenantId, Pageable pageable) {
        return certificateDb.findByTemplateIdAndTenantId(templateId, tenantId, pageable);
    }
}
