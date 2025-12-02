package com.kulturman.irembotest.api.controllers;

import com.kulturman.irembotest.AbstractIntegrationTest;
import com.kulturman.irembotest.domain.ports.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;

public class AuthControllerE2ETest extends AbstractIntegrationTest {
    @Autowired
    UserRepository userRepository;
}
