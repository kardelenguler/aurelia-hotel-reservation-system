package com.aurelia.backend.repository;

import com.aurelia.backend.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

    // Bir kullanıcının tüm rezervasyonları
    List<Reservation> findByUserIdOrderByCheckInDateDesc(Integer userId);

    // Bu oda, bu tarihlerde iptal edilmemiş bir rezervasyonla çakışıyor mu?
    @Query("""
            SELECT COUNT(r) > 0
            FROM Reservation r
            WHERE r.room.id = :roomId
              AND r.status <> 'cancelled'
              AND r.checkInDate < :checkOut
              AND r.checkOutDate > :checkIn
            """)
    boolean existsOverlapping(@Param("roomId") Integer roomId,
                              @Param("checkIn") LocalDate checkIn,
                              @Param("checkOut") LocalDate checkOut);
}