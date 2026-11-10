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

    // Belirli durumdaki rezervasyon sayısı (ör. pending)
    long countByStatus(String status);

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

    // Bu odanın devam eden veya gelecek aktif rezervasyonu var mı?
    @Query("""
            SELECT COUNT(r) > 0
            FROM Reservation r
            WHERE r.room.id = :roomId
              AND r.status IN ('pending', 'confirmed', 'checked_in')
              AND r.checkOutDate > :today
            """)
    boolean hasActiveOrUpcomingReservations(@Param("roomId") Integer roomId,
                                            @Param("today") LocalDate today);

    // Bugün dolu olan oda sayısı (onaylı veya giriş yapılmış, tarihleri bugünü kapsayan)
    @Query("""
            SELECT COUNT(DISTINCT r.room.id)
            FROM Reservation r
            WHERE r.status IN ('confirmed', 'checked_in')
              AND r.checkInDate <= :today
              AND r.checkOutDate > :today
            """)
    long countOccupiedRooms(@Param("today") LocalDate today);

    // Bugün gelecekler: girişi bugün, onaylı, henüz giriş yapmamış
    @Query("""
            SELECT r
            FROM Reservation r
            WHERE r.checkInDate = :today
              AND r.status = 'confirmed'
            ORDER BY r.room.roomNumber
            """)
    List<Reservation> findArrivals(@Param("today") LocalDate today);

    // Bugün ayrılacaklar: çıkışı bugün, şu an otelde
    @Query("""
            SELECT r
            FROM Reservation r
            WHERE r.checkOutDate = :today
              AND r.status = 'checked_in'
            ORDER BY r.room.roomNumber
            """)
    List<Reservation> findDepartures(@Param("today") LocalDate today);
}