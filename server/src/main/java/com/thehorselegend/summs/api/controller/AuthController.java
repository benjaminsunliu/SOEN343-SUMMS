package com.thehorselegend.summs.api.controller;

import com.thehorselegend.summs.api.dto.AuthResponse;
import com.thehorselegend.summs.api.dto.CreateCityProviderRequest;
import com.thehorselegend.summs.api.dto.LoginRequest;
import com.thehorselegend.summs.api.dto.RegisterRequest;
import com.thehorselegend.summs.application.service.AuthService;
import com.thehorselegend.summs.infrastructure.persistence.UserEntity;
import com.thehorselegend.summs.infrastructure.persistence.UserRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;


    public AuthController(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/admin/city-providers")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public AuthResponse createCityProvider(@Valid @RequestBody CreateCityProviderRequest request) {
        return authService.createCityProviderAccount(request.name(), request.email(), request.password());
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request, HttpSession session) {
        AuthResponse response = authService.login(request);
        UserEntity user = userRepository.findByEmail(response.email())
                .orElseThrow(() -> new RuntimeException("User not found"));
        session.setAttribute("user", user);
        return response;
    }
}