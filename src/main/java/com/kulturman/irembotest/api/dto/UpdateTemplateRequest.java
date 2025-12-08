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
@Schema(description = "Request to update an existing certificate template's name and/or content")
public class UpdateTemplateRequest {
    @NotBlank(message = "Template name is required")
    @Schema(
        description = "Updated name of the certificate template",
        example = "Advanced Completion Certificate"
    )
    private String name;

    @NotBlank(message = "Template content is required")
    @Schema(
        description = "Updated template content with variable placeholders using {{variableName}} syntax. Variables will be automatically re-extracted and updated.",
        example = "This is to certify that {{studentName}} has successfully completed the advanced {{courseName}} course with a score of {{score}}% on {{completionDate}}."
    )
    private String content;
}
