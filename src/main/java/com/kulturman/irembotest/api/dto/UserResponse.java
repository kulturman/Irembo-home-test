package com.kulturman.irembotest.api.dto;

import com.kulturman.irembotest.domain.entities.UserRole;
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
@Schema(description = "User account information returned from the API (excludes sensitive data like password)")
public class UserResponse {

    @Schema(description = "Unique identifier of the user", example = "123e4567-e89b-12d3-a456-426614174000")
    private UUID id;

    @Schema(description = "User's email address", example = "john.doe@example.com")
    private String email;

    @Schema(description = "User's full name", example = "John Doe")
    private String name;

    @Schema(description = "User's role in the system", example = "USER")
    private UserRole role;

    @Schema(description = "Tenant ID associated with this user", example = "b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a11")
    private UUID tenantId;

    @Schema(description = "Timestamp when the user account was created")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when the user account was last updated")
    private LocalDateTime updatedAt;
}
