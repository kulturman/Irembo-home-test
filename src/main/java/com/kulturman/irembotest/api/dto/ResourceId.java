package com.kulturman.irembotest.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resource identifier response containing the ID of a newly created resource")
public record ResourceId(
    @Schema(
        description = "Unique identifier of the created resource (UUID format)",
        example = "123e4567-e89b-12d3-a456-426614174000"
    )
    String id
) {
}
