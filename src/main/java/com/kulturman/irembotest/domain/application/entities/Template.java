package com.kulturman.irembotest.domain.application.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Template {
    private UUID tenantId;
    private UUID id;
    private String content;
    private String name;
    private String variables;
}
