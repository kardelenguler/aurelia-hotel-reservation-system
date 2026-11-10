package com.aurelia.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

// Rezervasyon oluştururken dışarıdan gelecek veri (sadece gerekli alanlar)
public record CreateReservationRequest(

        @NotNull(message = "Kullanıcı seçilmelidir.")
        Integer userId,

        @NotNull(message = "Oda seçilmelidir.")
        Integer roomId,

        @NotNull(message = "Giriş tarihi zorunludur.")
        LocalDate checkInDate,

        @NotNull(message = "Çıkış tarihi zorunludur.")
        LocalDate checkOutDate,

        @NotNull(message = "Kişi sayısı zorunludur.")
        @Min(value = 1, message = "Kişi sayısı en az 1 olmalıdır.")
        Integer guestCount
) {
}