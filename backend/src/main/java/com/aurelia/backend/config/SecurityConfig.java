package com.aurelia.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // REST API'de form kullanmadığımız için CSRF koruması gerekmiyor
                .csrf(csrf -> csrf.disable())
                // Oturum (session) tutmuyoruz; ileride her istekte token gelecek
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // ŞİMDİLİK her şey açık — Adım 26'da kilitleyeceğiz
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                // Tarayıcının açtığı varsayılan giriş pencerelerini kapat
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable());

        return http.build();
    }

    // Şifreleri BCrypt ile şifreler (tek yönlü, geri çözülemez)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}