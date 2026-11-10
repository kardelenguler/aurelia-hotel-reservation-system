package com.aurelia.backend.repository;

import com.aurelia.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    // E-postaya göre kullanıcı bul (giriş için)
    Optional<User> findByEmail(String email);

    // Bu e-posta ile kayıt var mı? (kayıt olurken)
    boolean existsByEmail(String email);
}