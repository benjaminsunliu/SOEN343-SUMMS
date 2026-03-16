package com.thehorselegend.summs.application.service;

import com.thehorselegend.summs.api.dto.AuthResponse;
import com.thehorselegend.summs.api.dto.LoginRequest;
import com.thehorselegend.summs.api.dto.RegisterRequest;
import com.thehorselegend.summs.domain.user.User;
import com.thehorselegend.summs.domain.user.UserFactory;
import com.thehorselegend.summs.infrastructure.persistence.UserEntity;
import com.thehorselegend.summs.infrastructure.persistence.UserMapper;
import com.thehorselegend.summs.infrastructure.persistence.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email already in use.");
        }

        String hashedPassword = passwordEncoder.encode(request.password());

        User user = UserFactory.createCitizen(
                request.name().trim(),
                normalizedEmail,
                hashedPassword
        );

        UserEntity savedEntity = userRepository.save(UserMapper.toEntity(user));
        User savedUser = UserMapper.toDomain(savedEntity);

        return new AuthResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole().name(),
                "Registration successful"
        );
    }

    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        UserEntity entity = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials."));

        if (!passwordEncoder.matches(request.password(), entity.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials.");
        }

        User user = UserMapper.toDomain(entity);

        return new AuthResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name(),
                "Login successful"
        );
    }
}