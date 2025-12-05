package com.kulturman.irembotest.infrastructure.persistence;

import com.kulturman.irembotest.domain.entities.User;
import com.kulturman.irembotest.domain.ports.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@AllArgsConstructor
public class JpaUserRepository implements UserRepository {
    private final UserDb userDb;

    @Override
    public Optional<User> findByEmail(String email) {
        return userDb.findByEmail(email);
    }

    @Override
    public User save(User user) {
        return userDb.save(user);
    }

    @Override
    public Page<User> findAllByTenantId(UUID tenantId, Pageable pageable) {
        return userDb.findAllByTenantId(tenantId, pageable);
    }

    @Override
    public Page<User> findAll(Pageable pageable) {
        return userDb.findAll(pageable);
    }
}
