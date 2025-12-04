package com.kulturman.irembotest.domain.application;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "Request to generate a certificate from a template by providing variable values")
public class GenerateCertificateRequest {
    @NotNull(message = "Template ID is required")
    @Schema(
        description = "Unique identifier of the template to use for certificate generation",
        example = "123e4567-e89b-12d3-a456-426614174000",
        required = true
    )
    private UUID templateId;

    @NotNull(message = "Variables are required")
    @Schema(
        description = "Map of variable names to values that will replace placeholders in the template content. Must include all variables defined in the template.",
        example = "{\"studentName\": \"John Doe\", \"courseName\": \"Java Programming\", \"completionDate\": \"2024-12-04\"}",
        required = true
    )
    private Map<String, String> variables;
}
