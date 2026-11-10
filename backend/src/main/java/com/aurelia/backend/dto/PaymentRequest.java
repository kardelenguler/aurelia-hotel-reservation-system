package com.aurelia.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

// Ödeme yaparken gelen veri — tutarı kullanıcı değil sistem belirler
public record PaymentRequest(

        @NotBlank(message = "Ödeme yöntemi zorunludur.")
        @Pattern(regexp = "card|cash", message = "Ödeme yöntemi 'card' veya 'cash' olmalıdır.")
        String method
) {
}