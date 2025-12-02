package com.kulturman.irembotest.infrastructure.configuration.security;

import com.kulturman.irembotest.domain.entities.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SecurityTenancyProviderTest {

    private SecurityTenancyProvider tenancyProvider;
    private UUID testTenantId;
    private User testUser;

    @BeforeEach
    void setUp() {
        tenancyProvider = new SecurityTenancyProvider();
        testTenantId = UUID.randomUUID();
        testUser = User.builder()
                .id(UUID.randomUUID())
                .tenantId(testTenantId)
                .email("test@example.com")
                .password("encoded-password")
                .name("Test User")
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldReturnTenantIdFromAuthenticatedUser() {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(testUser, null, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UUID tenantId = tenancyProvider.getCurrentTenantId();

        assertEquals(testTenantId, tenantId);
    }

    @Test
    void shouldReturnDifferentTenantIdsForDifferentUsers() {
        UUID tenant1 = UUID.randomUUID();
        UUID tenant2 = UUID.randomUUID();

        User user1 = User.builder()
                .id(UUID.randomUUID())
                .tenantId(tenant1)
                .email("user1@example.com")
                .password("password")
                .build();

        User user2 = User.builder()
                .id(UUID.randomUUID())
                .tenantId(tenant2)
                .email("user2@example.com")
                .password("password")
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user1, null, null));
        UUID result1 = tenancyProvider.getCurrentTenantId();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user2, null, null));
        UUID result2 = tenancyProvider.getCurrentTenantId();

        assertEquals(tenant1, result1);
        assertEquals(tenant2, result2);
        assertNotEquals(result1, result2);
    }

    @Test
    void shouldThrowExceptionWhenNoAuthentication() {
        SecurityContextHolder.clearContext();

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> tenancyProvider.getCurrentTenantId()
        );

        assertTrue(exception.getMessage().contains("No authenticated user found"));
    }

    @Test
    void shouldThrowExceptionWhenPrincipalIsNotUser() {
        String invalidPrincipal = "not-a-user-object";
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(invalidPrincipal, null, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> tenancyProvider.getCurrentTenantId()
        );

        assertTrue(exception.getMessage().contains("Principal is not a User instance"));
    }
}
