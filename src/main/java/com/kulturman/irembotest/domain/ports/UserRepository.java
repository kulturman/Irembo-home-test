package com.kulturman.irembotest.domain.ports;


import com.kulturman.irembotest.domain.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    Optional<User> findByEmail(String email);
    User save(User user);
    Page<User> findAllByTenantId(UUID tenantId, Pageable pageable);
    Page<User> findAll(Pageable pageable);
}
