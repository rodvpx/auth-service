package com.backendlab.authservice.service;

import com.backendlab.authservice.dto.RegisterRequest;
import com.backendlab.authservice.entity.Role;
import com.backendlab.authservice.entity.User;
import com.backendlab.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public User register(RegisterRequest request) {

        if (repository.existsByEmail(request.email())) {
            throw new RuntimeException("Email já cadastrado");
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .createdAt(LocalDateTime.now())
                .role(Role.USER)
                .build();

        return repository.save(user);
    }
}
