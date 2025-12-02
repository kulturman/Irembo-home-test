package com.kulturman.irembotest.domain.ports;


import com.kulturman.irembotest.domain.entities.User;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findByEmail(String email);
}
