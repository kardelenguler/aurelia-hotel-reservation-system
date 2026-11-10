package com.aurelia.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Kayıt olurken gelen veri — "role" alanı YOK, herkes müşteri olarak kaydolur
public record RegisterRequest(

        @NotBlank(message = "Ad soyad zorunludur.")
        @Size(max = 100, message = "Ad soyad en fazla 100 karakter olabilir.")
        String fullName,

        @NotBlank(message = "E-posta zorunludur.")
        @Email(message = "Geçerli bir e-posta adresi girin.")
        @Size(max = 255, message = "E-posta en fazla 255 karakter olabilir.")
        String email,

        @NotBlank(message = "Şifre zorunludur.")
        @Size(min = 8, max = 72, message = "Şifre 8 ile 72 karakter arasında olmalıdır.")
        String password
) {
}