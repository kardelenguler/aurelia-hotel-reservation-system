package com.aurelia.backend.service;

import com.aurelia.backend.dto.CancellationPreview;
import com.aurelia.backend.dto.CreateReservationRequest;
import com.aurelia.backend.entity.Payment;
import com.aurelia.backend.entity.Reservation;
import com.aurelia.backend.entity.ReservationStatus;
import com.aurelia.backend.entity.Room;
import com.aurelia.backend.entity.User;
import com.aurelia.backend.exception.ConflictException;
import com.aurelia.backend.exception.ForbiddenException;
import com.aurelia.backend.exception.ResourceNotFoundException;
import com.aurelia.backend.exception.UnauthorizedException;
import com.aurelia.backend.repository.PaymentRepository;
import com.aurelia.backend.repository.ReservationRepository;
import com.aurelia.backend.repository.RoomRepository;
import com.aurelia.backend.repository.UserRepository;
import com.aurelia.backend.security.AuthUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);

    // İptal politikası: girişe 7+ gün → %100 iade, 1-6 gün → %50 iade, giriş günü → iptal yok
    private static final int FULL_REFUND_MIN_DAYS = 7;
    private static final int FULL_REFUND_PERCENT = 100;
    private static final int PARTIAL_REFUND_PERCENT = 50;

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final RoomRepository roomRepository;
    private final PaymentRepository paymentRepository;

    public ReservationService(ReservationRepository reservationRepository,
                              UserRepository userRepository,
                              RoomRepository roomRepository,
                              PaymentRepository paymentRepository) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.roomRepository = roomRepository;
        this.paymentRepository = paymentRepository;
    }

    // ---------- Okuma ----------

    // Admin: tüm rezervasyonlar
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    // Admin: bir kullanıcının rezervasyonları
    public List<Reservation> getReservationsByUser(Integer userId) {
        return reservationRepository.findByUserIdOrderByCheckInDateDesc(userId);
    }

    // Giriş yapmış kullanıcının kendi rezervasyonları
    public List<Reservation> getMyReservations(AuthUser authUser) {
        return reservationRepository.findByUserIdOrderByCheckInDateDesc(authUser.id());
    }

    // Tek rezervasyon: sahibi veya admin görebilir
    public Reservation getReservationForUser(Integer id, AuthUser authUser) {
        Reservation reservation = findReservation(id);
        ensureOwnerOrAdmin(reservation, authUser);
        return reservation;
    }

    // ---------- Oluşturma ----------

    @Transactional
    public Reservation createReservation(CreateReservationRequest request, AuthUser authUser) {
        LocalDate checkIn = request.checkInDate();
        LocalDate checkOut = request.checkOutDate();

        // 1) Tarih kontrolleri
        if (checkIn.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Giriş tarihi geçmiş bir tarih olamaz.");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("Çıkış tarihi, giriş tarihinden sonra olmalıdır.");
        }

        // 2) Kullanıcı token'dan
        User user = userRepository.findById(authUser.id())
                .orElseThrow(() -> new UnauthorizedException("Kullanıcı bulunamadı. Lütfen tekrar giriş yapın."));

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

        // 6) Fiyat: gece sayısı × gecelik fiyat
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
        reservation.setStatus(ReservationStatus.PENDING);

        try {
            Reservation saved = reservationRepository.saveAndFlush(reservation);
            log.info("Yeni rezervasyon: id={}, kullanıcı={}, oda={}, {} → {}",
                    saved.getId(), user.getId(), room.getRoomNumber(), checkIn, checkOut);
            return saved;
        } catch (DataIntegrityViolationException ex) {
            throw new ConflictException("Bu oda seçilen tarihlerde dolu.");
        }
    }

    // ---------- İptal ----------

    // İptal etmeden önce: iptal edilebilir mi, ne kadar iade alınır?
    public CancellationPreview previewCancellation(Integer id, AuthUser authUser) {
        Reservation reservation = getReservationForUser(id, authUser);
        return evaluateCancellation(reservation);
    }

    @Transactional
    public Reservation cancelReservation(Integer id, AuthUser authUser) {
        Reservation reservation = getReservationForUser(id, authUser);
        CancellationPreview preview = evaluateCancellation(reservation);

        if (!preview.cancellable()) {
            throw new IllegalArgumentException(preview.message());
        }

        // Silmiyoruz, durumunu değiştiriyoruz → geçmiş kayıt korunur
        reservation.setStatus(ReservationStatus.CANCELLED);

        // Ödeme yapılmışsa politikaya göre iade
        paymentRepository.findByReservationId(id).ifPresent(payment -> {
            if (Payment.STATUS_PAID.equals(payment.getStatus())) {
                payment.setRefundAmount(preview.refundAmount());
                payment.setStatus(Payment.STATUS_REFUNDED);
                log.info("İade: rezervasyon={}, ödenen={}, iade=%{} → {}",
                        id, payment.getAmount(), preview.refundPercent(), preview.refundAmount());
            }
        });

        log.info("Rezervasyon iptal edildi: id={}, işlemi yapan={}", id, authUser.id());
        return reservation;
    }

    // İptal kurallarını tek yerde hesaplar (hem önizleme hem iptal kullanır)
    private CancellationPreview evaluateCancellation(Reservation reservation) {
        String status = reservation.getStatus();
        long daysUntilCheckIn = ChronoUnit.DAYS.between(LocalDate.now(), reservation.getCheckInDate());

        BigDecimal paidAmount = paymentRepository.findByReservationId(reservation.getId())
                .filter(payment -> Payment.STATUS_PAID.equals(payment.getStatus()))
                .map(Payment::getAmount)
                .orElse(BigDecimal.ZERO);

        if (ReservationStatus.CANCELLED.equals(status)) {
            return notCancellable(daysUntilCheckIn, paidAmount, "Bu rezervasyon zaten iptal edilmiş.");
        }
        if (ReservationStatus.CHECKED_IN.equals(status) || ReservationStatus.CHECKED_OUT.equals(status)) {
            return notCancellable(daysUntilCheckIn, paidAmount,
                    "Giriş yapılmış veya tamamlanmış rezervasyon iptal edilemez.");
        }
        if (daysUntilCheckIn <= 0) {
            return notCancellable(daysUntilCheckIn, paidAmount, "Giriş günü gelmiş rezervasyon iptal edilemez.");
        }

        int refundPercent = daysUntilCheckIn >= FULL_REFUND_MIN_DAYS
                ? FULL_REFUND_PERCENT
                : PARTIAL_REFUND_PERCENT;

        BigDecimal refundAmount = paidAmount
                .multiply(BigDecimal.valueOf(refundPercent))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        String message;
        if (paidAmount.signum() == 0) {
            message = "Ödeme yapılmadığı için iade söz konusu değildir.";
        } else if (refundPercent == FULL_REFUND_PERCENT) {
            message = "Girişe 7 gün veya daha fazla kaldığı için ödemenin tamamı iade edilir.";
        } else {
            message = "Girişe 7 günden az kaldığı için ödemenin %50'si iade edilir.";
        }

        return new CancellationPreview(true, daysUntilCheckIn, refundPercent, paidAmount, refundAmount, message);
    }

    private CancellationPreview notCancellable(long days, BigDecimal paidAmount, String message) {
        return new CancellationPreview(false, days, 0, paidAmount, BigDecimal.ZERO, message);
    }

    // ---------- Check-in / Check-out (admin) ----------

    @Transactional
    public Reservation checkIn(Integer id) {
        Reservation reservation = findReservation(id);
        LocalDate today = LocalDate.now();

        if (!ReservationStatus.CONFIRMED.equals(reservation.getStatus())) {
            throw new IllegalArgumentException(
                    "Sadece onaylanmış (ödemesi yapılmış) rezervasyonlarda giriş yapılabilir.");
        }
        if (today.isBefore(reservation.getCheckInDate())) {
            throw new IllegalArgumentException(
                    "Giriş tarihi henüz gelmedi (giriş: " + reservation.getCheckInDate() + ").");
        }
        if (!today.isBefore(reservation.getCheckOutDate())) {
            throw new IllegalArgumentException("Çıkış tarihi geçmiş rezervasyona giriş yapılamaz.");
        }

        reservation.setStatus(ReservationStatus.CHECKED_IN);
        log.info("Check-in: rezervasyon={}, oda={}", id, reservation.getRoom().getRoomNumber());
        return reservation;
    }

    @Transactional
    public Reservation checkOut(Integer id) {
        Reservation reservation = findReservation(id);

        if (!ReservationStatus.CHECKED_IN.equals(reservation.getStatus())) {
            throw new IllegalArgumentException(
                    "Sadece giriş yapılmış rezervasyonlarda çıkış yapılabilir.");
        }

        reservation.setStatus(ReservationStatus.CHECKED_OUT);
        log.info("Check-out: rezervasyon={}, oda={}", id, reservation.getRoom().getRoomNumber());
        return reservation;
    }

    // ---------- Yardımcılar ----------

    private Reservation findReservation(Integer id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rezervasyon bulunamadı (id: " + id + ")."));
    }

    private void ensureOwnerOrAdmin(Reservation reservation, AuthUser authUser) {
        boolean isOwner = reservation.getUser().getId().equals(authUser.id());
        if (!isOwner && !authUser.isAdmin()) {
            throw new ForbiddenException("Bu rezervasyona erişim yetkiniz yok.");
        }
    }
}