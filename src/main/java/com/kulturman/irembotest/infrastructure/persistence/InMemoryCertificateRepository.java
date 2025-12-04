package com.kulturman.irembotest.infrastructure.persistence;

import com.kulturman.irembotest.domain.entities.Certificate;
import com.kulturman.irembotest.domain.ports.CertificateRepository;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @Override
    public Optional<Certificate> findByDownloadToken(String downloadToken) {
        return savedCertificates.stream()
            .filter(c -> downloadToken.equals(c.getDownloadToken()))
            .findFirst();
    }

    @Override
    public Page<Certificate> findByTemplateIdAndTenantId(UUID templateId, UUID tenantId, Pageable pageable) {
        List<Certificate> filtered = savedCertificates.stream()
            .filter(c -> c.getTemplate().getId().equals(templateId) && c.getTenantId().equals(tenantId))
            .sorted((c1, c2) -> c2.getCreatedAt().compareTo(c1.getCreatedAt())) // Sort by createdAt DESC
            .collect(Collectors.toList());

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());

        List<Certificate> pageContent = start < filtered.size() ? filtered.subList(start, end) : List.of();
        return new PageImpl<>(pageContent, pageable, filtered.size());
    }
}
