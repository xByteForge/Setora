package com.byteforge.setorabackend.auth;

import com.byteforge.setorabackend.security.JwtConfig;
import com.byteforge.setorabackend.user.User;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtEncoder jwtEncoder;

    @Value("${jwt.access-minutes:15}")
    private long accessMinutes;

    @Value("${jwt.refresh-days:7}")
    private long refreshDays;

    public long getAccessTokenLifetimeSeconds() {
        return Duration.ofMinutes(accessMinutes).toSeconds();
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(JwtConfig.ISSUER)
                .subject(String.valueOf(user.getId()))
                .issuedAt(now)
                .expiresAt(now.plus(Duration.ofMinutes(accessMinutes)))
                .id(UUID.randomUUID().toString())
                .claim("token_type", "access")
                .claim("role", user.getRole().name())
                .claim("token_version", user.getTokenVersion())
                .build();

        return encode(claims);
    }

    public IssuedRefreshToken generateRefreshToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(Duration.ofDays(refreshDays));
        String tokenId = UUID.randomUUID().toString();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(JwtConfig.ISSUER)
                .subject(String.valueOf(user.getId()))
                .issuedAt(now)
                .expiresAt(expiresAt)
                .id(tokenId)
                .claim("token_type", "refresh")
                .claim("token_version", user.getTokenVersion())
                .build();

        return new IssuedRefreshToken(encode(claims), tokenId, expiresAt);
    }

    private String encode(JwtClaimsSet claims) {
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public record IssuedRefreshToken(String token, String tokenId, Instant expiresAt) {}
}
