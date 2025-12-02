package com.kulturman.irembotest.domain.application;

import com.kulturman.irembotest.api.dto.AuthResponse;
import com.kulturman.irembotest.api.dto.LoginRequest;
import com.kulturman.irembotest.domain.entities.User;
import com.kulturman.irembotest.domain.ports.UserRepository;
import com.kulturman.irembotest.infrastructure.configuration.security.JwtService;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(user);
        return new AuthResponse(token);
    }
}
