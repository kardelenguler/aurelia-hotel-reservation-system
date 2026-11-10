package com.aurelia.backend.dto;

import java.math.BigDecimal;

// İptal etmeden önce müşteriye gösterilecek bilgi (onay penceresinde)
public record CancellationPreview(
        boolean cancellable,
        long daysUntilCheckIn,
        int refundPercent,
        BigDecimal paidAmount,
        BigDecimal refundAmount,
        String message
) {
}