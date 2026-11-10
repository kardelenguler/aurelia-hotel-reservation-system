package com.aurelia.backend.dto;

// Giriş başarılı olunca dönen cevap
public record AuthResponse(
        String token,
        String tokenType,
        long expiresIn,
        Integer userId,
        String fullName,
        String email,
        String role
) {
}