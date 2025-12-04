package com.kulturman.irembotest.api.dto;

import com.kulturman.irembotest.domain.entities.CertificateStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificateResponse {
    private UUID id;
    private UUID templateId;
    private String templateName;
    private String variables;
    private CertificateStatus status;
    private String downloadToken;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
