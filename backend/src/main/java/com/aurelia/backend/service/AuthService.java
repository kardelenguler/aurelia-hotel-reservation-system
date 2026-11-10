package com.aurelia.backend.service;

import com.aurelia.backend.dto.RegisterRequest;
import com.aurelia.backend.entity.User;
import com.aurelia.backend.exception.ConflictException;
import com.aurelia.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(RegisterRequest request) {
        // E-postayı standart hale getir: boşlukları sil, küçük harf yap
        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Bu e-posta adresiyle zaten bir hesap var.");
        }

        User user = new User();
        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        // Şifreyi ASLA düz kaydetmiyoruz
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        // Rol her zaman müşteri — admin'i sadece mevcut admin/veritabanı belirler
        user.setRole("customer");

        User saved = userRepository.save(user);
        log.info("Yeni kullanıcı kaydı: id={}, email={}", saved.getId(), saved.getEmail());
        return saved;
    }
}