package com.kulturman.irembotest.domain.application;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerateCertificateRequest {
    private UUID templateId;
    private Map<String, String> variables;
}
