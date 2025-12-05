package com.kulturman.irembotest.domain.application;

import com.kulturman.irembotest.api.dto.CreateUserRequest;
import com.kulturman.irembotest.api.dto.ResourceId;
import com.kulturman.irembotest.api.dto.UserResponse;
import com.kulturman.irembotest.domain.entities.User;
import com.kulturman.irembotest.domain.exceptions.UserAlreadyExistsException;
import com.kulturman.irembotest.domain.ports.TenancyProvider;
import com.kulturman.irembotest.domain.ports.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@AllArgsConstructor
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TenancyProvider tenancyProvider;

    public ResourceId createUser(CreateUserRequest request) {
        var tenantId = tenancyProvider.getCurrentTenantId();

        userRepository.findByEmail(request.getEmail()).ifPresent(u -> {
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        });

        User user = User.builder()
            .id(UUID.randomUUID())
            .tenantId(tenantId)
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .name(request.getName())
            .role(request.getRole())
            .build();

        User savedUser = userRepository.save(user);

        return new ResourceId(savedUser.getId().toString());
    }

    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::mapToResponse);
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
            .id(user.getId())
            .email(user.getEmail())
            .name(user.getName())
            .role(user.getRole())
            .tenantId(user.getTenantId())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
    }
}
