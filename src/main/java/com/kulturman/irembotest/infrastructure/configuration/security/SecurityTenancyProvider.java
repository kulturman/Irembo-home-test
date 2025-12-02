package com.kulturman.irembotest.infrastructure.configuration.security;

import com.kulturman.irembotest.domain.entities.User;
import com.kulturman.irembotest.domain.ports.TenancyProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SecurityTenancyProvider implements TenancyProvider {

    @Override
    public UUID getCurrentTenantId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found. Cannot determine tenant context.");
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof User)) {
            throw new IllegalStateException("Principal is not a User instance. Cannot determine tenant context.");
        }

        User user = (User) principal;
        return user.getTenantId();
    }
}
