package com.aurelia.backend.service;

import com.aurelia.backend.dto.CreateReservationRequest;
import com.aurelia.backend.entity.Reservation;
import com.aurelia.backend.entity.Room;
import com.aurelia.backend.entity.User;
import com.aurelia.backend.exception.ConflictException;
import com.aurelia.backend.exception.ResourceNotFoundException;
import com.aurelia.backend.repository.ReservationRepository;
import com.aurelia.backend.repository.RoomRepository;
import com.aurelia.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              UserRepository userRepository,
                              RoomRepository roomRepository) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
    }

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    public Reservation getReservationById(Integer id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rezervasyon bulunamadı (id: " + id + ")."));
    }

    public List<Reservation> getReservationsByUser(Integer userId) {
        return reservationRepository.findByUserIdOrderByCheckInDateDesc(userId);
    }

    @Transactional
    public Reservation createReservation(CreateReservationRequest request) {
        LocalDate checkIn = request.checkInDate();
        LocalDate checkOut = request.checkOutDate();

        // 1) Tarih kontrolleri
        if (checkIn.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Giriş tarihi geçmiş bir tarih olamaz.");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("Çıkış tarihi, giriş tarihinden sonra olmalıdır.");
        }

        // 2) Kullanıcı var mı?
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Kullanıcı bulunamadı (id: " + request.userId() + ")."));

        // 3) Oda var mı, aktif mi?
        Room room = roomRepository.findById(request.roomId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Oda bulunamadı (id: " + request.roomId() + ")."));
        if (!Boolean.TRUE.equals(room.getIsActive())) {
            throw new IllegalArgumentException("Bu oda şu anda rezervasyona kapalı.");
        }

        // 4) Kapasite yetiyor mu?
        int capacity = room.getRoomType().getCapacity();
        if (request.guestCount() > capacity) {
            throw new IllegalArgumentException(
                    "Kişi sayısı oda kapasitesini aşıyor (en fazla " + capacity + " kişi).");
        }

        // 5) Oda o tarihlerde boş mu?
        if (reservationRepository.existsOverlapping(room.getId(), checkIn, checkOut)) {
            throw new ConflictException("Bu oda seçilen tarihlerde dolu.");
        }

        // 6) Fiyat hesapla: gece sayısı × gecelik fiyat
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);
        BigDecimal totalPrice = room.getRoomType().getPricePerNight()
                .multiply(BigDecimal.valueOf(nights));

        // 7) Kaydet
        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setRoom(room);
        reservation.setCheckInDate(checkIn);
        reservation.setCheckOutDate(checkOut);
        reservation.setGuestCount(request.guestCount());
        reservation.setTotalPrice(totalPrice);
        reservation.setStatus("pending");

        try {
            Reservation saved = reservationRepository.saveAndFlush(reservation);
            log.info("Yeni rezervasyon: id={}, oda={}, {} → {}",
                    saved.getId(), room.getRoomNumber(), checkIn, checkOut);
            return saved;
        } catch (DataIntegrityViolationException ex) {
            // Aynı anda başka biri aynı odayı aldıysa veritabanı kuralı yakalar
            throw new ConflictException("Bu oda seçilen tarihlerde dolu.");
        }
    }
}