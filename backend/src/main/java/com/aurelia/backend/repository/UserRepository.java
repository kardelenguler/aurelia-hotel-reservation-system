package com.aurelia.backend.repository;

import com.aurelia.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    // E-postaya göre kullanıcı bul (ileride giriş için lazım)
    Optional<User> findByEmail(String email);
}