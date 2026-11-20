package com.byteforge.setorabackend.auth;

import com.byteforge.setorabackend.security.JwtConfig;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
public class RefreshJwtVerifier {
    private final JwtDecoder decoder;

    public RefreshJwtVerifier(SecretKey secretKey) {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        OAuth2TokenValidator<Jwt> refreshOnly = jwt -> {
            boolean valid = "refresh".equals(jwt.getClaimAsString("token_type")) && jwt.getId() != null && !jwt.getId().isBlank() && jwt.getSubject() != null && jwt.getExpiresAt() != null;

            return valid ? OAuth2TokenValidatorResult.success() : OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "Invalid refresh token", null)
            );
        };

        jwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefaultWithIssuer(JwtConfig.ISSUER), refreshOnly));
        this.decoder = jwtDecoder;
    }

    public Jwt verify(String token) {
        return decoder.decode(token);
    }
}
