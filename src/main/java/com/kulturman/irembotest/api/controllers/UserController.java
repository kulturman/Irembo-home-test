package com.kulturman.irembotest.api.controllers;

import com.kulturman.irembotest.api.dto.CreateUserRequest;
import com.kulturman.irembotest.api.dto.ResourceId;
import com.kulturman.irembotest.api.dto.UserResponse;
import com.kulturman.irembotest.domain.application.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
@Tag(name = "User Management", description = "Admin-only endpoints for managing user accounts and roles")
public class UserController {
    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Create a new user (Admin only)",
        description = "Create a new user account with specified role. Only administrators can create users with any role (USER or ADMIN)."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "User created successfully, returns the new user ID",
            content = @Content(schema = @Schema(implementation = ResourceId.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request body or user already exists",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - valid JWT token required",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Admin role required",
            content = @Content
        )
    })
    public ResponseEntity<ResourceId> createUser(@Valid @RequestBody CreateUserRequest request) {
        ResourceId response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "Get all users (Admin only)",
        description = "Retrieve a paginated list of all users in the current tenant. Results can be sorted and paginated."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Users retrieved successfully",
            content = @Content(schema = @Schema(implementation = Page.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - valid JWT token required",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Forbidden - Admin role required",
            content = @Content
        )
    })
    public ResponseEntity<Page<UserResponse>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }
}
