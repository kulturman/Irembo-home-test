package com.kulturman.irembotest.domain.ports;

import com.kulturman.irembotest.domain.application.entities.Certificate;

public interface CertificateRepository {
    void save(Certificate certificate);
}