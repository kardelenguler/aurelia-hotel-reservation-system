package com.aurelia.backend.service;

import com.aurelia.backend.dto.RoomRequest;
import com.aurelia.backend.entity.Room;
import com.aurelia.backend.entity.RoomType;
import com.aurelia.backend.exception.ConflictException;
import com.aurelia.backend.exception.ResourceNotFoundException;
import com.aurelia.backend.repository.ReservationRepository;
import com.aurelia.backend.repository.RoomRepository;
import com.aurelia.backend.repository.RoomTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class RoomService {

    private static final Logger log = LoggerFactory.getLogger(RoomService.class);

    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final ReservationRepository reservationRepository;

    public RoomService(RoomRepository roomRepository,
                       RoomTypeRepository roomTypeRepository,
                       ReservationRepository reservationRepository) {
        this.roomRepository = roomRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.reservationRepository = reservationRepository;
    }

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public List<Room> getActiveRooms() {
        return roomRepository.findByIsActiveTrue();
    }

    public Room getRoomById(Integer id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Oda bulunamadı (id: " + id + ")."));
    }

    public List<Room> searchAvailableRooms(LocalDate checkIn, LocalDate checkOut, int guests) {
        if (checkIn == null || checkOut == null) {
            throw new IllegalArgumentException("Giriş ve çıkış tarihi zorunludur.");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Giriş tarihi geçmiş bir tarih olamaz.");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("Çıkış tarihi, giriş tarihinden sonra olmalıdır.");
        }
        if (guests < 1) {
            throw new IllegalArgumentException("Kişi sayısı en az 1 olmalıdır.");
        }
        return roomRepository.findAvailableRooms(checkIn, checkOut, guests);
    }

    @Transactional
    public Room createRoom(RoomRequest request) {
        String roomNumber = request.roomNumber().trim();

        if (roomRepository.existsByRoomNumber(roomNumber)) {
            throw new ConflictException("Bu oda numarası zaten kullanılıyor: " + roomNumber);
        }
        RoomType roomType = findRoomType(request.roomTypeId());

        Room room = new Room();
        room.setRoomNumber(roomNumber);
        room.setRoomType(roomType);
        room.setFloor(request.floor());
        room.setIsActive(true);

        Room saved = roomRepository.save(room);
        log.info("Yeni oda eklendi: {} ({})", saved.getRoomNumber(), roomType.getName());
        return saved;
    }

    @Transactional
    public Room updateRoom(Integer id, RoomRequest request) {
        Room room = getRoomById(id);
        String roomNumber = request.roomNumber().trim();

        if (roomRepository.existsByRoomNumberAndIdNot(roomNumber, id)) {
            throw new ConflictException("Bu oda numarası başka bir odada kullanılıyor: " + roomNumber);
        }
        RoomType roomType = findRoomType(request.roomTypeId());

        room.setRoomNumber(roomNumber);
        room.setRoomType(roomType);
        room.setFloor(request.floor());

        log.info("Oda güncellendi: id={}, numara={}", id, roomNumber);
        return room;
    }

    @Transactional
    public Room deactivateRoom(Integer id) {
        Room room = getRoomById(id);

        if (!Boolean.TRUE.equals(room.getIsActive())) {
            throw new IllegalArgumentException("Oda zaten pasif.");
        }
        if (reservationRepository.hasActiveOrUpcomingReservations(id, LocalDate.now())) {
            throw new ConflictException(
                    "Bu odada devam eden veya gelecek rezervasyonlar var. Önce onları iptal edin.");
        }

        room.setIsActive(false);
        log.info("Oda pasife alındı: {}", room.getRoomNumber());
        return room;
    }

    @Transactional
    public Room activateRoom(Integer id) {
        Room room = getRoomById(id);

        if (Boolean.TRUE.equals(room.getIsActive())) {
            throw new IllegalArgumentException("Oda zaten aktif.");
        }

        room.setIsActive(true);
        log.info("Oda aktifleştirildi: {}", room.getRoomNumber());
        return room;
    }

    private RoomType findRoomType(Integer roomTypeId) {
        return roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Oda tipi bulunamadı (id: " + roomTypeId + ")."));
    }
}