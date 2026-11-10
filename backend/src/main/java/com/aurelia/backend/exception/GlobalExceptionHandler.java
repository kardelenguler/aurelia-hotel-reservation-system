package com.aurelia.backend.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

// Tüm hataları tek yerden yakalar, kullanıcıya anlaşılır mesaj döner
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // Kural hataları (ör. tarih hatalı, kapasite aşıldı) → 400
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Geçersiz istek: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // Kayıt bulunamadı → 404
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        log.warn("Bulunamadı: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // Çakışma (ör. oda dolu) → 409
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(ConflictException ex) {
        log.warn("Çakışma: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    // Veritabanı kuralı ihlali (ör. aynı anda iki kişi aynı odayı aldı) → 409
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Veritabanı kuralı ihlali: {}", ex.getMostSpecificCause().getMessage());
        return buildResponse(HttpStatus.CONFLICT,
                "İşlem veritabanı kurallarına uymuyor. Lütfen bilgileri kontrol edin.");
    }

    // @Valid kontrolleri (ör. boş alan) → 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(" "));
        log.warn("Doğrulama hatası: {}", message);
        return buildResponse(HttpStatus.BAD_REQUEST, message);
    }

    // Bozuk JSON veya yanlış tarih formatı → 400
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadable(HttpMessageNotReadableException ex) {
        log.warn("Okunamayan istek gövdesi: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST,
                "İstek verisi okunamadı. JSON ve tarih formatını (YYYY-AA-GG) kontrol edin.");
    }

    // Zorunlu parametre eksik → 400
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParam(MissingServletRequestParameterException ex) {
        log.warn("Eksik parametre: {}", ex.getParameterName());
        return buildResponse(HttpStatus.BAD_REQUEST,
                "Eksik parametre: " + ex.getParameterName());
    }

    // Yanlış formatta parametre → 400
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("Hatalı parametre formatı: {}", ex.getName());
        return buildResponse(HttpStatus.BAD_REQUEST,
                "Hatalı format: " + ex.getName() + " (tarih formatı: YYYY-AA-GG)");
    }

    // Olmayan adres → 404
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoResource(NoResourceFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, "Böyle bir adres bulunamadı.");
    }

    // Beklenmeyen tüm hatalar → 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneral(Exception ex) {
        log.error("Beklenmeyen hata", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Beklenmeyen bir hata oluştu. Lütfen tekrar deneyin.");
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}