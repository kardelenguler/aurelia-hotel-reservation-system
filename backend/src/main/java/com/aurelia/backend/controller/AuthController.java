package com.aurelia.backend.controller;

import com.aurelia.backend.dto.AuthResponse;
import com.aurelia.backend.dto.LoginRequest;
import com.aurelia.backend.dto.RegisterRequest;
import com.aurelia.backend.entity.User;
import com.aurelia.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // Kayıt ol
    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    // Giriş yap → token al
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    // Token sahibi kim? (giriş yapmış kullanıcının bilgileri)
    @GetMapping("/me")
    public User me(@AuthenticationPrincipal Jwt jwt) {
        return authService.getCurrentUser(jwt);
    }
}