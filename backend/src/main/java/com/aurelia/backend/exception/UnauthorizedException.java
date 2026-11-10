package com.aurelia.backend.exception;

// Giriş yapılmamış veya bilgiler hatalı (401)
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}