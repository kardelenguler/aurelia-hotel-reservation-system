package com.aurelia.backend.controller;

import com.aurelia.backend.dto.CreateReservationRequest;
import com.aurelia.backend.entity.Reservation;
import com.aurelia.backend.security.AuthUser;
import com.aurelia.backend.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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

    // 🛎️ Admin: tüm rezervasyonlar
    @GetMapping
    public List<Reservation> getAllReservations() {
        return reservationService.getAllReservations();
    }

    // 🛎️ Admin: bir kullanıcının rezervasyonları
    @GetMapping("/user/{userId}")
    public List<Reservation> getReservationsByUser(@PathVariable Integer userId) {
        return reservationService.getReservationsByUser(userId);
    }

    // 👤 Benim rezervasyonlarım
    @GetMapping("/my")
    public List<Reservation> getMyReservations(@AuthenticationPrincipal Jwt jwt) {
        return reservationService.getMyReservations(AuthUser.from(jwt));
    }

    // 👤 Tek rezervasyon (sahibi veya admin)
    @GetMapping("/{id}")
    public Reservation getReservationById(@PathVariable Integer id, @AuthenticationPrincipal Jwt jwt) {
        return reservationService.getReservationForUser(id, AuthUser.from(jwt));
    }

    // 👤 Yeni rezervasyon (kullanıcı token'dan)
    @PostMapping
    public ResponseEntity<Reservation> createReservation(@Valid @RequestBody CreateReservationRequest request,
                                                         @AuthenticationPrincipal Jwt jwt) {
        Reservation created = reservationService.createReservation(request, AuthUser.from(jwt));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // 👤 İptal (sahibi veya admin)
    @PatchMapping("/{id}/cancel")
    public Reservation cancelReservation(@PathVariable Integer id, @AuthenticationPrincipal Jwt jwt) {
        return reservationService.cancelReservation(id, AuthUser.from(jwt));
    }

    // 🛎️ Admin: check-in
    @PatchMapping("/{id}/check-in")
    public Reservation checkIn(@PathVariable Integer id) {
        return reservationService.checkIn(id);
    }

    // 🛎️ Admin: check-out
    @PatchMapping("/{id}/check-out")
    public Reservation checkOut(@PathVariable Integer id) {
        return reservationService.checkOut(id);
    }
}