package com.kulturman.irembotest.infrastructure.persistence;

import com.kulturman.irembotest.domain.entities.User;
import com.kulturman.irembotest.domain.ports.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@AllArgsConstructor
public class JpaUserRepository implements UserRepository {
    private final UserDb userDb;

    @Override
    public Optional<User> findByEmail(String email) {
        return userDb.findByEmail(email);
    }
}
