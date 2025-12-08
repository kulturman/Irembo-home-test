package com.kulturman.irembotest.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new certificate template with name and content")
public class CreateTemplateRequest {
    @NotBlank(message = "Template name is required")
    @Schema(
        description = "Name of the certificate template",
        example = "Completion Certificate"
    )
    private String name;

    @NotBlank(message = "Template content is required")
    @Schema(
        description = "Template content with variable placeholders using {{variableName}} syntax. Variables will be automatically extracted and stored.",
        example = "This is to certify that {{studentName}} has successfully completed {{courseName}} on {{completionDate}}."
    )
    private String content;
}
