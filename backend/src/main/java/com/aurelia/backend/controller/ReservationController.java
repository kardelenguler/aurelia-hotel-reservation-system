package com.aurelia.backend.controller;

import com.aurelia.backend.dto.CreateReservationRequest;
import com.aurelia.backend.entity.Reservation;
import com.aurelia.backend.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    // Tüm rezervasyonlar (ileride sadece admin)
    @GetMapping
    public List<Reservation> getAllReservations() {
        return reservationService.getAllReservations();
    }

    // Tek rezervasyon: /api/reservations/1
    @GetMapping("/{id}")
    public Reservation getReservationById(@PathVariable Integer id) {
        return reservationService.getReservationById(id);
    }

    // Kullanıcının rezervasyonları: /api/reservations/user/2
    @GetMapping("/user/{userId}")
    public List<Reservation> getReservationsByUser(@PathVariable Integer userId) {
        return reservationService.getReservationsByUser(userId);
    }

    // Yeni rezervasyon oluştur
    @PostMapping
    public ResponseEntity<Reservation> createReservation(
            @Valid @RequestBody CreateReservationRequest request) {
        Reservation created = reservationService.createReservation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}