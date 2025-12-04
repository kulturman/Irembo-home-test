package com.kulturman.irembotest.domain.ports;

import com.kulturman.irembotest.domain.entities.Certificate;

import java.util.Optional;
import java.util.UUID;

public interface CertificateRepository {
    void save(Certificate certificate);
    Optional<Certificate> findById(UUID certificateId);
    Optional<Certificate> findByIdAndTenantId(UUID certificateId, UUID tenantId);
    Optional<Certificate> findByDownloadToken(String downloadToken);
}
