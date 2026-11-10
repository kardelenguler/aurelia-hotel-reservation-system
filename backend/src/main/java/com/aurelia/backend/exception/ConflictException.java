package com.aurelia.backend.exception;

// Çakışma olduğunda, ör. oda o tarihlerde dolu (409)
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}