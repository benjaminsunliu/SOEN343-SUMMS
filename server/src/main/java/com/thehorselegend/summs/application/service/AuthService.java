package com.thehorselegend.summs.application.service;

import com.thehorselegend.summs.api.dto.AuthResponse;
import com.thehorselegend.summs.api.dto.LoginRequest;
import com.thehorselegend.summs.api.dto.RegisterRequest;
import com.thehorselegend.summs.application.service.auth.strategy.UserRegistrationStrategy;
import com.thehorselegend.summs.domain.user.User;
import com.thehorselegend.summs.domain.user.UserRole;
import com.thehorselegend.summs.infrastructure.security.JwtService;
import com.thehorselegend.summs.infrastructure.persistence.UserEntity;
import com.thehorselegend.summs.infrastructure.persistence.UserMapper;
import com.thehorselegend.summs.infrastructure.persistence.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

@Service
public class AuthService {

        private static final EnumSet<UserRole> SELF_REGISTRABLE_ROLES = EnumSet.of(
            UserRole.CITIZEN,
            UserRole.PROVIDER,
            UserRole.CITY_PROVIDER);
    private static final UserRole ADMIN_CREATABLE_ROLE = UserRole.CITY_PROVIDER;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final Map<UserRole, UserRegistrationStrategy> registrationStrategies;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            List<UserRegistrationStrategy> strategyList) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.registrationStrategies = indexStrategies(strategyList);
    }

    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email already in use.");
        }

        String hashedPassword = passwordEncoder.encode(request.password());
        UserRole requestedRole = resolveRequestedRole(request.role());
        UserRegistrationStrategy strategy = resolveStrategy(requestedRole);

        User user = strategy.create(
                request.name().trim(),
                normalizedEmail,
                hashedPassword);

        UserEntity savedEntity = userRepository.save(UserMapper.toEntity(user));
        User savedUser = UserMapper.toDomain(savedEntity);

        return new AuthResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole().name(),
                "Registration successful",
                jwtService.generateToken(savedUser));
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
                "Login successful",
                jwtService.generateToken(user));
    }

    public AuthResponse createCityProviderAccount(String name, String email, String password) {
        String normalizedEmail = email.trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email already in use.");
        }

        UserRegistrationStrategy strategy = resolveStrategy(ADMIN_CREATABLE_ROLE);
        String hashedPassword = passwordEncoder.encode(password);

        User user = strategy.create(
                name.trim(),
                normalizedEmail,
                hashedPassword);

        UserEntity savedEntity = userRepository.save(UserMapper.toEntity(user));
        User savedUser = UserMapper.toDomain(savedEntity);

        return new AuthResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole().name(),
                "City provider account created",
                jwtService.generateToken(savedUser));
    }

    private UserRole resolveRequestedRole(UserRole requestedRole) {
        UserRole roleToUse = requestedRole == null ? UserRole.CITIZEN : requestedRole;
        if (!SELF_REGISTRABLE_ROLES.contains(roleToUse)) {
            throw new IllegalArgumentException("Selected role cannot be self-registered.");
        }
        return roleToUse;
    }

    private UserRegistrationStrategy resolveStrategy(UserRole role) {
        UserRegistrationStrategy strategy = registrationStrategies.get(role);
        if (strategy == null) {
            throw new IllegalArgumentException("No registration strategy found for role: " + role);
        }
        return strategy;
    }

    private Map<UserRole, UserRegistrationStrategy> indexStrategies(List<UserRegistrationStrategy> strategyList) {
        EnumMap<UserRole, UserRegistrationStrategy> indexed = new EnumMap<>(UserRole.class);
        for (UserRegistrationStrategy strategy : strategyList) {
            indexed.put(strategy.supportedRole(), strategy);
        }
        return Map.copyOf(indexed);
    }
}