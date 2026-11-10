package com.aurelia.backend.service;

import com.aurelia.backend.entity.Room;
import com.aurelia.backend.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public List<Room> getActiveRooms() {
        return roomRepository.findByIsActiveTrue();
    }

    public List<Room> searchAvailableRooms(LocalDate checkIn, LocalDate checkOut, int guests) {
        // Giriş kontrolleri
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
}