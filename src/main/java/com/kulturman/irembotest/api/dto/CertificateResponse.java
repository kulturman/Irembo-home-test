package com.kulturman.irembotest.api.dto;

import com.kulturman.irembotest.domain.entities.CertificateStatus;
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
@Schema(description = "Certificate response containing generated certificate details and metadata")
public class CertificateResponse {
    @Schema(
        description = "Unique identifier of the certificate",
        example = "123e4567-e89b-12d3-a456-426614174000",
        required = true
    )
    private UUID id;

    @Schema(
        description = "Unique identifier of the template used to generate this certificate",
        example = "987e6543-e21b-12d3-a456-426614174999",
        required = true
    )
    private UUID templateId;

    @Schema(
        description = "Name of the template used to generate this certificate",
        example = "Completion Certificate",
        required = true
    )
    private String templateName;

    @Schema(
        description = "JSON string containing the variable values used to generate this certificate",
        example = "{\"studentName\":\"John Doe\",\"courseName\":\"Java Programming\",\"completionDate\":\"2024-12-04\"}",
        required = true
    )
    private String variables;

    @Schema(
        description = "Current status of the certificate",
        example = "GENERATED",
        required = true,
        allowableValues = {"PENDING", "GENERATED", "REVOKED"}
    )
    private CertificateStatus status;

    @Schema(
        description = "Unique token for downloading the certificate PDF without authentication",
        example = "abc123def456ghi789jkl012mno345pqr",
        required = true
    )
    private String downloadToken;

    @Schema(
        description = "Timestamp when the certificate was generated",
        example = "2024-12-04T10:30:00",
        required = true
    )
    private LocalDateTime createdAt;

    @Schema(
        description = "Timestamp when the certificate was last updated",
        example = "2024-12-04T10:30:00",
        required = true
    )
    private LocalDateTime updatedAt;
}
