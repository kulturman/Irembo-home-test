package com.kulturman.irembotest.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Login request containing user credentials for authentication")
public class LoginRequest {
    @NotBlank
    @Email
    @Schema(
        description = "User's email address used as username",
        example = "user@example.com",
        required = true,
        format = "email"
    )
    private String email;

    @NotBlank
    @Schema(
        description = "User's password for authentication",
        example = "SecurePassword123!",
        required = true,
        format = "password"
    )
    private String password;
}
