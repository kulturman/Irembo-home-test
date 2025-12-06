package com.kulturman.irembotest.infrastructure.configuration.security;

import java.util.UUID;

public record JwtPrincipal(
    UUID userId,
    String email,
    UUID tenantId,
    String role
) {
    public static JwtPrincipal from(JwtService.JwtClaims claims) {
        return new JwtPrincipal(
            claims.userId(),
            claims.email(),
            claims.tenantId(),
            claims.role()
        );
    }
}
