package com.aurelia.backend.security;

import com.aurelia.backend.exception.UnauthorizedException;
import org.springframework.security.oauth2.jwt.Jwt;

// Token'dan okunan giriş yapmış kullanıcı: id + rol
public record AuthUser(Integer id, String role) {

    public static final String ROLE_CUSTOMER = "customer";
    public static final String ROLE_RECEPTIONIST = "receptionist";
    public static final String ROLE_ADMIN = "admin";

    public static AuthUser from(Jwt jwt) {
        if (jwt == null) {
            throw new UnauthorizedException("Bu işlem için giriş yapmanız gerekiyor.");
        }
        return new AuthUser(Integer.valueOf(jwt.getSubject()), jwt.getClaimAsString("role"));
    }

    // Yönetici
    public boolean isAdmin() {
        return ROLE_ADMIN.equals(role);
    }

    // Resepsiyon
    public boolean isReceptionist() {
        return ROLE_RECEPTIONIST.equals(role);
    }

    // Otel personeli (resepsiyon veya yönetici)
    public boolean isStaff() {
        return isAdmin() || isReceptionist();
    }
}