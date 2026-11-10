package com.aurelia.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// Oda ekleme / düzenleme için gelen veri
public record RoomRequest(

        @NotBlank(message = "Oda numarası zorunludur.")
        @Size(max = 10, message = "Oda numarası en fazla 10 karakter olabilir.")
        String roomNumber,

        @NotNull(message = "Oda tipi seçilmelidir.")
        Integer roomTypeId,

        @NotNull(message = "Kat bilgisi zorunludur.")
        @Min(value = 0, message = "Kat 0 veya daha büyük olmalıdır.")
        Integer floor
) {
}