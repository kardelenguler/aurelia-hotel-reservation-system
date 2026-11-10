package com.aurelia.backend.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.time.LocalDateTime;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 🌐 Herkese açık
                        .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/room-types").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/rooms/*").permitAll() // active, available, {id}
                        .requestMatchers("/error").permitAll()

                        // 🛎️ Sadece admin
                        .requestMatchers("/api/admin/**").hasRole("admin")
                        .requestMatchers("/api/rooms", "/api/rooms/**").hasRole("admin")
                        .requestMatchers(HttpMethod.GET, "/api/reservations").hasRole("admin")
                        .requestMatchers("/api/reservations/user/**").hasRole("admin")
                        .requestMatchers(HttpMethod.PATCH,
                                "/api/reservations/*/check-in",
                                "/api/reservations/*/check-out").hasRole("admin")

                        // 👤 Geri kalan her şey: giriş yapmış kullanıcı
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler()))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint())
                        .accessDeniedHandler(accessDeniedHandler()))
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable());

        return http.build();
    }

    // Token'daki "role" bilgisini Spring'in anlayacağı yetkiye çevirir: admin → ROLE_admin
    private JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName("role");
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }

    // Token yok / geçersiz / süresi dolmuş → 401
    private AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> writeJson(response, 401, "Unauthorized",
                "Bu işlem için giriş yapmanız gerekiyor. Token eksik, geçersiz veya süresi dolmuş.");
    }

    // Giriş yapılmış ama rol yetmiyor → 403
    private AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> writeJson(response, 403, "Forbidden",
                "Bu işlem için yetkiniz yok.");
    }

    private void writeJson(HttpServletResponse response, int status, String error, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String body = "{"
                + "\"timestamp\":\"" + LocalDateTime.now() + "\","
                + "\"status\":" + status + ","
                + "\"error\":\"" + error + "\","
                + "\"message\":\"" + message + "\""
                + "}";
        response.getWriter().write(body);
    }

    // Şifreleri BCrypt ile şifreler
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}