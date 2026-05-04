package com.backendlab.authservice.service;

import com.backendlab.authservice.dto.AuthResponse;
import com.backendlab.authservice.dto.LoginRequest;
import com.backendlab.authservice.entity.User;
import com.backendlab.authservice.exception.InvalidCredentialsException;
import com.backendlab.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;


    public AuthResponse login(LoginRequest request) {

        User user = repository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(user.getEmail());

        return new AuthResponse(token);
    }
}
