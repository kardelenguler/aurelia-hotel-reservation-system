package com.aurelia.backend.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
public class JwtConfig {

    // local.properties'teki gizli anahtarı okuyup kontrol eder
    @Bean
    public SecretKey jwtSecretKey(@Value("${aurelia.jwt.secret}") String secret) {
        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(secret.trim());
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException(
                    "aurelia.jwt.secret geçerli bir Base64 değeri değil. local.properties dosyasını kontrol edin.", ex);
        }
        if (keyBytes.length < 32) {
            throw new IllegalStateException("aurelia.jwt.secret en az 32 byte (256 bit) olmalıdır.");
        }
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    // Token üretici (imzalar)
    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return new NimbusJwtEncoder(new ImmutableSecret<>(jwtSecretKey));
    }

    // Token doğrulayıcı (imzayı ve süreyi kontrol eder)
    @Bean
    public JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {
        return NimbusJwtDecoder.withSecretKey(jwtSecretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }
}