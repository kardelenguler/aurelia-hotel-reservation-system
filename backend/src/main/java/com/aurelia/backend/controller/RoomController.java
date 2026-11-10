package com.aurelia.backend.controller;

import com.aurelia.backend.dto.RoomRequest;
import com.aurelia.backend.entity.Room;
import com.aurelia.backend.service.RoomService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    // Tüm odalar (admin)
    @GetMapping
    public List<Room> getAllRooms() {
        return roomService.getAllRooms();
    }

    // Sadece aktif odalar
    @GetMapping("/active")
    public List<Room> getActiveRooms() {
        return roomService.getActiveRooms();
    }

    // Müsait oda arama
    // Örnek: /api/rooms/available?checkIn=2026-12-20&checkOut=2026-12-24&guests=2
    @GetMapping("/available")
    public List<Room> searchAvailableRooms(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(defaultValue = "1") int guests) {
        return roomService.searchAvailableRooms(checkIn, checkOut, guests);
    }

    // Tek oda: /api/rooms/1
    @GetMapping("/{id}")
    public Room getRoomById(@PathVariable Integer id) {
        return roomService.getRoomById(id);
    }

    // Oda ekle (admin)
    @PostMapping
    public ResponseEntity<Room> createRoom(@Valid @RequestBody RoomRequest request) {
        Room created = roomService.createRoom(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Oda düzenle (admin)
    @PutMapping("/{id}")
    public Room updateRoom(@PathVariable Integer id, @Valid @RequestBody RoomRequest request) {
        return roomService.updateRoom(id, request);
    }

    // Odayı pasife al (admin)
    @PatchMapping("/{id}/deactivate")
    public Room deactivateRoom(@PathVariable Integer id) {
        return roomService.deactivateRoom(id);
    }

    // Odayı tekrar aktifleştir (admin)
    @PatchMapping("/{id}/activate")
    public Room activateRoom(@PathVariable Integer id) {
        return roomService.activateRoom(id);
    }
}