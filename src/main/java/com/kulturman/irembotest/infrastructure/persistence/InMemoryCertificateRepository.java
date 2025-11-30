package com.kulturman.irembotest.infrastructure.persistence;

import com.kulturman.irembotest.domain.application.entities.Certificate;
import com.kulturman.irembotest.domain.ports.CertificateRepository;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class InMemoryCertificateRepository implements CertificateRepository {
    public final List<Certificate> savedCertificates = new ArrayList<>();

    @Override
    public void save(Certificate certificate) {
        savedCertificates.add(certificate);
    }
}
