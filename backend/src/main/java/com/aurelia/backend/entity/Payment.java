package com.aurelia.backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "payments")
public class Payment {

    public static final String STATUS_PAID = "paid";
    public static final String STATUS_REFUNDED = "refunded";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Bir rezervasyon → bir ödeme
    @JsonIgnore
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reservation_id", nullable = false, unique = true)
    private Reservation reservation;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    // card / cash
    @Column(name = "method", nullable = false, length = 20)
    private String method;

    // paid / refunded
    @Column(name = "status", nullable = false, length = 20)
    private String status = STATUS_PAID;

    // İade edilen tutar (tam iade = amount, %50 iade = amount / 2, iade yok = 0)
    @Column(name = "refund_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal refundAmount = BigDecimal.ZERO;

    @Column(name = "paid_at", nullable = false, updatable = false)
    private OffsetDateTime paidAt;

    @PrePersist
    protected void onCreate() {
        this.paidAt = OffsetDateTime.now();
    }

    public Payment() {
    }

    public Integer getId() {
        return id;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    // JSON'da tüm rezervasyon yerine sadece id görünsün
    public Integer getReservationId() {
        return reservation != null ? reservation.getId() : null;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }

    public OffsetDateTime getPaidAt() {
        return paidAt;
    }
}