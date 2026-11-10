package com.aurelia.backend.service;

import com.aurelia.backend.dto.DashboardResponse;
import com.aurelia.backend.entity.Reservation;
import com.aurelia.backend.entity.ReservationStatus;
import com.aurelia.backend.repository.PaymentRepository;
import com.aurelia.backend.repository.ReservationRepository;
import com.aurelia.backend.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
public class DashboardService {

    private final RoomRepository roomRepository;
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;

    public DashboardService(RoomRepository roomRepository,
                            ReservationRepository reservationRepository,
                            PaymentRepository paymentRepository) {
        this.roomRepository = roomRepository;
        this.reservationRepository = reservationRepository;
        this.paymentRepository = paymentRepository;
    }

    public DashboardResponse getDashboard() {
        LocalDate today = LocalDate.now();

        // Doluluk
        long activeRooms = roomRepository.countByIsActiveTrue();
        long occupiedRooms = reservationRepository.countOccupiedRooms(today);
        double occupancyRate = activeRooms == 0
                ? 0.0
                : Math.round(occupiedRooms * 1000.0 / activeRooms) / 10.0; // 1 ondalık, ör. 30.0

        // Sayılar
        long inHouseCount = reservationRepository.countByStatus(ReservationStatus.CHECKED_IN);
        long pendingPaymentCount = reservationRepository.countByStatus(ReservationStatus.PENDING);

        // Bu ayın geliri: ayın 1'i 00:00 → sonraki ayın 1'i 00:00
        OffsetDateTime monthStart = today.withDayOfMonth(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toOffsetDateTime();
        OffsetDateTime nextMonthStart = monthStart.plusMonths(1);
        BigDecimal revenueThisMonth = paymentRepository.sumPaidBetween(monthStart, nextMonthStart);

        // Bugünkü listeler
        List<Reservation> arrivals = reservationRepository.findArrivals(today);
        List<Reservation> departures = reservationRepository.findDepartures(today);

        return new DashboardResponse(
                today,
                activeRooms,
                occupiedRooms,
                occupancyRate,
                inHouseCount,
                pendingPaymentCount,
                revenueThisMonth,
                arrivals,
                departures
        );
    }
}
