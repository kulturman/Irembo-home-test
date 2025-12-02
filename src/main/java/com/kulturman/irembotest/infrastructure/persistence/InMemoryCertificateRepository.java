package com.kulturman.irembotest.infrastructure.persistence;

import com.kulturman.irembotest.domain.entities.Certificate;
import com.kulturman.irembotest.domain.ports.CertificateRepository;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Getter
public class InMemoryCertificateRepository implements CertificateRepository {
    public final List<Certificate> savedCertificates = new ArrayList<>();

    @Override
    public void save(Certificate certificate) {
        // Remove existing certificate with same ID before adding
        savedCertificates.removeIf(c -> c.getId().equals(certificate.getId()));
        savedCertificates.add(certificate);
    }

    @Override
    public Optional<Certificate> findById(UUID certificateId) {
        return savedCertificates.stream()
            .filter(c -> c.getId().equals(certificateId))
            .findFirst();
    }

    @Override
    public Optional<Certificate> findByIdAndTenantId(UUID certificateId, UUID tenantId) {
        return savedCertificates.stream()
            .filter(c -> c.getId().equals(certificateId) && c.getTenantId().equals(tenantId))
            .findFirst();
    }
}
