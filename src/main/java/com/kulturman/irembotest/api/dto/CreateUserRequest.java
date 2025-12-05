package com.kulturman.irembotest.api.dto;

import com.kulturman.irembotest.domain.entities.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request to create a new user account with specified role and credentials")
public class CreateUserRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Schema(description = "User's email address (will be used as username for login)",
            example = "john.doe@example.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Schema(description = "User's password (will be hashed before storage)",
            example = "SecurePassword123!")
    private String password;

    @NotBlank(message = "Name is required")
    @Schema(description = "User's full name for display purposes",
            example = "John Doe")
    private String name;

    @NotNull(message = "Role is required")
    @Schema(description = "User's role determining their access level in the system",
            example = "USER")
    private UserRole role;
}
