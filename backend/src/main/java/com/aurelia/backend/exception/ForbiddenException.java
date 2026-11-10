package com.aurelia.backend.exception;

// Giriş yapılmış ama bu işleme yetki yok (403)
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}