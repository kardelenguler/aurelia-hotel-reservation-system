package com.aurelia.backend.service;

import com.aurelia.backend.dto.AuthResponse;
import com.aurelia.backend.dto.LoginRequest;
import com.aurelia.backend.dto.RegisterRequest;
import com.aurelia.backend.entity.User;
import com.aurelia.backend.exception.ConflictException;
import com.aurelia.backend.exception.UnauthorizedException;
import com.aurelia.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
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
        // Rol her zaman müşteri — admin'i sadece veritabanı belirler
        user.setRole("customer");

        User saved = userRepository.save(user);
        log.info("Yeni kullanıcı kaydı: id={}, email={}", saved.getId(), saved.getEmail());
        return saved;
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();

        // Güvenlik: e-posta mı şifre mi yanlış, bunu söylemiyoruz (tahmin etmeyi zorlaştırır)
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("E-posta veya şifre hatalı."));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            log.warn("Hatalı giriş denemesi: {}", email);
            throw new UnauthorizedException("E-posta veya şifre hatalı.");
        }

        String token = jwtService.generateToken(user);
        log.info("Giriş yapıldı: id={}, email={}", user.getId(), email);

        return new AuthResponse(
                token,
                "Bearer",
                jwtService.getExpirationSeconds(),
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole()
        );
    }

    // Token'daki kullanıcıyı bul
    public User getCurrentUser(Jwt jwt) {
        if (jwt == null) {
            throw new UnauthorizedException("Bu işlem için giriş yapmanız gerekiyor.");
        }
        Integer userId = Integer.valueOf(jwt.getSubject());
        return userRepository.findById(userId)
                .orElseThrow(() -> new UnauthorizedException("Kullanıcı bulunamadı. Lütfen tekrar giriş yapın."));
    }
}