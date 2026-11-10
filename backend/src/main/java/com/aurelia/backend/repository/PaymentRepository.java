package com.aurelia.backend.repository;

import com.aurelia.backend.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    // Sorguları açıkça yazıyoruz: p.reservation.id (ilişki üzerinden)
    @Query("SELECT p FROM Payment p WHERE p.reservation.id = :reservationId")
    Optional<Payment> findByReservationId(@Param("reservationId") Integer reservationId);

    @Query("SELECT COUNT(p) > 0 FROM Payment p WHERE p.reservation.id = :reservationId")
    boolean existsByReservationId(@Param("reservationId") Integer reservationId);

    // Net gelir: alınan ödemeler − yapılan iadeler (%50 iadede kalan yarısı gelir sayılır)
    @Query("""
            SELECT COALESCE(SUM(p.amount - p.refundAmount), 0)
            FROM Payment p
            WHERE p.paidAt >= :start
              AND p.paidAt < :end
            """)
    BigDecimal sumPaidBetween(@Param("start") OffsetDateTime start,
                              @Param("end") OffsetDateTime end);
}