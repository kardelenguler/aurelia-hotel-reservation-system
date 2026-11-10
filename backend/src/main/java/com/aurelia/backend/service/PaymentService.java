package com.aurelia.backend.service;

import com.aurelia.backend.dto.PaymentRequest;
import com.aurelia.backend.entity.Payment;
import com.aurelia.backend.entity.Reservation;
import com.aurelia.backend.entity.ReservationStatus;
import com.aurelia.backend.exception.ConflictException;
import com.aurelia.backend.exception.ResourceNotFoundException;
import com.aurelia.backend.repository.PaymentRepository;
import com.aurelia.backend.repository.ReservationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final ReservationRepository reservationRepository;

    public PaymentService(PaymentRepository paymentRepository,
                          ReservationRepository reservationRepository) {
        this.paymentRepository = paymentRepository;
        this.reservationRepository = reservationRepository;
    }

    public Payment getPaymentByReservation(Integer reservationId) {
        return paymentRepository.findByReservationId(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Bu rezervasyon için ödeme kaydı bulunamadı (id: " + reservationId + ")."));
    }

    @Transactional
    public Payment pay(Integer reservationId, PaymentRequest request) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Rezervasyon bulunamadı (id: " + reservationId + ")."));

        if (paymentRepository.existsByReservationId(reservationId)) {
            throw new ConflictException("Bu rezervasyon için zaten ödeme yapılmış.");
        }
        if (!ReservationStatus.PENDING.equals(reservation.getStatus())) {
            throw new IllegalArgumentException(
                    "Sadece ödeme bekleyen rezervasyonlar için ödeme yapılabilir.");
        }

        Payment payment = new Payment();
        payment.setReservation(reservation);
        payment.setAmount(reservation.getTotalPrice());
        payment.setMethod(request.method());
        payment.setStatus(Payment.STATUS_PAID);

        reservation.setStatus(ReservationStatus.CONFIRMED);

        Payment saved = paymentRepository.save(payment);
        log.info("Ödeme alındı: rezervasyon={}, tutar={}, yöntem={}",
                reservationId, saved.getAmount(), saved.getMethod());
        return saved;
    }
}