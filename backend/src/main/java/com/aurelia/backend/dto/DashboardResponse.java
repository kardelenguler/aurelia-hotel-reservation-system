package com.aurelia.backend.dto;

import com.aurelia.backend.entity.Reservation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

// Admin paneli özet verileri
public record DashboardResponse(
        LocalDate date,
        long activeRooms,
        long occupiedRooms,
        double occupancyRate,
        long inHouseCount,
        long pendingPaymentCount,
        BigDecimal revenueThisMonth,
        List<Reservation> arrivalsToday,
        List<Reservation> departuresToday
) {
}