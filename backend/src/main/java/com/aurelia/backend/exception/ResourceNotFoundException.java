package com.aurelia.backend.exception;

// Aranan kayıt bulunamadığında (404)
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}