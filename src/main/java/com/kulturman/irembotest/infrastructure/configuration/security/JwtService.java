package com.kulturman.irembotest.infrastructure.configuration.security;

import com.kulturman.irembotest.domain.entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class JwtService {
    private final SecretKey secretKey;
    private final long expirationTime;

    public record JwtClaims(String email, UUID tenantId, String role, UUID userId) {}

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration:86400000}") long expirationTime) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.expirationTime = expirationTime;
    }

    public String generateToken(User user) {
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId().toString())
                .claim("tenantId", user.getTenantId().toString())
                .claim("role", user.getRole().name())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(secretKey)
                .compact();
    }

    public Optional<JwtClaims> parseAndValidate(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return Optional.of(new JwtClaims(
                    claims.getSubject(),
                    UUID.fromString(claims.get("tenantId", String.class)),
                    claims.get("role", String.class),
                    UUID.fromString(claims.get("userId", String.class))
            ));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    // Deprecated - use parseAndValidate() instead for single-parse efficiency
    public boolean validateToken(String token) {
        return parseAndValidate(token).isPresent();
    }

    // Deprecated - use parseAndValidate() instead for single-parse efficiency
    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    // Deprecated - use parseAndValidate() instead for single-parse efficiency
    public UUID extractTenantId(String token) {
        String tenantId = getClaims(token).get("tenantId", String.class);
        return UUID.fromString(tenantId);
    }

    // Deprecated - use parseAndValidate() instead for single-parse efficiency
    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
