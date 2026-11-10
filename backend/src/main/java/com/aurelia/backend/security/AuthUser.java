package com.aurelia.backend.security;

import com.aurelia.backend.exception.UnauthorizedException;
import org.springframework.security.oauth2.jwt.Jwt;

// Token'dan okunan giriş yapmış kullanıcı: id + rol
public record AuthUser(Integer id, String role) {

    public static AuthUser from(Jwt jwt) {
        if (jwt == null) {
            throw new UnauthorizedException("Bu işlem için giriş yapmanız gerekiyor.");
        }
        return new AuthUser(Integer.valueOf(jwt.getSubject()), jwt.getClaimAsString("role"));
    }

    public boolean isAdmin() {
        return "admin".equals(role);
    }
}