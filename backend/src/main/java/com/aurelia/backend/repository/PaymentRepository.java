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

    // Belirli tarih aralığında alınan ödemelerin toplamı (iade edilenler hariç)
    @Query("""
            SELECT COALESCE(SUM(p.amount), 0)
            FROM Payment p
            WHERE p.status = 'paid'
              AND p.paidAt >= :start
              AND p.paidAt < :end
            """)
    BigDecimal sumPaidBetween(@Param("start") OffsetDateTime start,
                              @Param("end") OffsetDateTime end);
}