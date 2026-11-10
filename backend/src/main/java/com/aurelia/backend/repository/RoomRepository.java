package com.aurelia.backend.repository;

import com.aurelia.backend.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Integer> {

    // Sadece aktif odaları getir
    List<Room> findByIsActiveTrue();

    // Aktif oda sayısı (dashboard)
    long countByIsActiveTrue();

    // Bu oda numarası kullanılıyor mu? (yeni oda eklerken)
    boolean existsByRoomNumber(String roomNumber);

    // Bu oda numarası BAŞKA bir odada kullanılıyor mu? (düzenlerken)
    boolean existsByRoomNumberAndIdNot(String roomNumber, Integer id);

    // Müsait odalar: aktif + kapasitesi yeten + o tarihlerde çakışan rezervasyonu olmayan
    @Query(value = """
            SELECT r.*
            FROM rooms r
            JOIN room_types rt ON r.room_type_id = rt.id
            WHERE r.is_active = TRUE
              AND rt.capacity >= :guests
              AND NOT EXISTS (
                  SELECT 1
                  FROM reservations res
                  WHERE res.room_id = r.id
                    AND res.status <> 'cancelled'
                    AND res.check_in_date < :checkOut
                    AND res.check_out_date > :checkIn
              )
            ORDER BY r.room_number
            """, nativeQuery = true)
    List<Room> findAvailableRooms(@Param("checkIn") LocalDate checkIn,
                                  @Param("checkOut") LocalDate checkOut,
                                  @Param("guests") int guests);
}