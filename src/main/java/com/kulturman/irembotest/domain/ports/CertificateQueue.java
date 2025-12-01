package com.kulturman.irembotest.domain.ports;

import com.kulturman.irembotest.domain.application.entities.Certificate;

public interface CertificateQueue {
    void publish(Certificate certificate);
}
