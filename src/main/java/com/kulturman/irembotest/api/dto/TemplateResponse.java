package com.kulturman.irembotest.api.dto;

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
public class TemplateResponse {
    private UUID id;
    private String name;
    private String content;
    private String variables;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
