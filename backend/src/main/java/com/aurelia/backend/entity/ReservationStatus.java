package com.aurelia.backend.entity;

// Durum değerleri tek yerde — yazım hatasını önler
public final class ReservationStatus {

    public static final String PENDING = "pending";
    public static final String CONFIRMED = "confirmed";
    public static final String CHECKED_IN = "checked_in";
    public static final String CHECKED_OUT = "checked_out";
    public static final String CANCELLED = "cancelled";

    private ReservationStatus() {
    }
}