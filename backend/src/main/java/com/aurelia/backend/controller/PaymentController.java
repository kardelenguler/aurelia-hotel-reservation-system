package com.aurelia.backend.controller;

import com.aurelia.backend.dto.PaymentRequest;
import com.aurelia.backend.entity.Payment;
import com.aurelia.backend.security.AuthUser;
import com.aurelia.backend.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservations/{reservationId}/payment")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // Ödemeyi görüntüle
    @GetMapping
    public Payment getPayment(@PathVariable Integer reservationId, @AuthenticationPrincipal Jwt jwt) {
        return paymentService.getPaymentByReservation(reservationId, AuthUser.from(jwt));
    }

    // Ödeme yap
    @PostMapping
    public ResponseEntity<Payment> pay(@PathVariable Integer reservationId,
                                       @Valid @RequestBody PaymentRequest request,
                                       @AuthenticationPrincipal Jwt jwt) {
        Payment payment = paymentService.pay(reservationId, request, AuthUser.from(jwt));
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }
}