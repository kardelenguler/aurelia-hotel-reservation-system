package com.aurelia.backend.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "E-posta zorunludur.")
        String email,

        @NotBlank(message = "Şifre zorunludur.")
        String password
) {
}