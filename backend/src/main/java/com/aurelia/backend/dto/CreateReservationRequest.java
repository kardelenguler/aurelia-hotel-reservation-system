package com.aurelia.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

// userId YOK → kullanıcı token'dan alınır, kimse başkası adına rezervasyon yapamaz
public record CreateReservationRequest(

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