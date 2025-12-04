package com.kulturman.irembotest.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Certificate template response containing template details and metadata")
public class TemplateResponse {
    @Schema(
        description = "Unique identifier of the template",
        example = "123e4567-e89b-12d3-a456-426614174000",
        required = true
    )
    private UUID id;

    @Schema(
        description = "Name of the certificate template",
        example = "Completion Certificate",
        required = true
    )
    private String name;

    @Schema(
        description = "Template content with variable placeholders using {{variableName}} syntax",
        example = "This is to certify that {{studentName}} has successfully completed {{courseName}} on {{completionDate}}.",
        required = true
    )
    private String content;

    @Schema(
        description = "Comma-separated list of variables extracted from the template content",
        example = "studentName,courseName,completionDate",
        nullable = true
    )
    private String variables;

    @Schema(
        description = "Timestamp when the template was created",
        example = "2024-12-04T10:30:00",
        required = true
    )
    private LocalDateTime createdAt;

    @Schema(
        description = "Timestamp when the template was last updated",
        example = "2024-12-04T15:45:00",
        required = true
    )
    private LocalDateTime updatedAt;
}
