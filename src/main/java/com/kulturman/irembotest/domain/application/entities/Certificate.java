package com.kulturman.irembotest.domain.application.entities;

import com.kulturman.irembotest.domain.entities.CertificateStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Certificate {
    private UUID id;
    private UUID templateId;
    private String variables;
    private CertificateStatus status;
    private UUID tenantId;
}
