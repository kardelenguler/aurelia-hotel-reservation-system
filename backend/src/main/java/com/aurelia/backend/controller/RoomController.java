package com.aurelia.backend.controller;

import com.aurelia.backend.entity.Room;
import com.aurelia.backend.service.RoomService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
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

    // Tüm odalar
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
}