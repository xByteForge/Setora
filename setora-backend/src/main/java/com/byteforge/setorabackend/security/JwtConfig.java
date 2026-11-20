package com.byteforge.setorabackend.security;

import com.byteforge.setorabackend.auth.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
public class JwtConfig {
    public static final String ISSUER = "setora";

    @Bean
    public SecretKey jwtSecretKey(@Value("${jwt.secret}") String secret) {
        byte[] bytes = Base64.getDecoder().decode(secret);

        if (bytes.length < 32)
            throw new IllegalArgumentException("JWT secret must contain at least 32 bytes");

        return new SecretKeySpec(bytes, "HmacSHA256");
    }

    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return NimbusJwtEncoder.withSecretKey(jwtSecretKey)
                .algorithm(MacAlgorithm.HS256)
                .build();
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKey jwtSecretKey, UserRepository userRepository) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(jwtSecretKey)
                        .macAlgorithm(MacAlgorithm.HS256)
                        .build();

        OAuth2TokenValidator<Jwt> activeAccessToken = jwt -> {
            boolean valid = false;

            try {
                int userId = Integer.parseInt(jwt.getSubject());

                valid = "access".equals(jwt.getClaimAsString("token_type")) && userRepository.findById(userId)
                        .filter(user -> !user.isDeleted())
                        .filter(user -> user.getRole() != null)
                        .filter(user -> user.getRole().name().equals(jwt.getClaimAsString("role")))
                        .filter(user -> {
                            Object version = jwt.getClaims().get("token_version");

                            return version instanceof Number && ((Number) version).intValue() == user.getTokenVersion();
                        })
                        .isPresent();

            } catch (NumberFormatException | NullPointerException e) {
                valid = false;
            }

            return valid ? OAuth2TokenValidatorResult.success() : OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "Invalid or inactive account", null)
            );
        };

        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefaultWithIssuer(ISSUER), activeAccessToken));
        return decoder;
    }
}