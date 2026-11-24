package com.byteforge.setorabackend.auth.refresh_tokens;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
    SELECT rt
    FROM RefreshToken rt
    WHERE rt.tokenId = :tokenId
""")
    Optional<RefreshToken> findByTokenIdForUpdate(@Param("tokenId") String tokenId);

    @Modifying
    @Query("""
    UPDATE RefreshToken rt
    SET rt.revokedAt = :now
    WHERE rt.user.id = :userId AND rt.revokedAt IS NULL
""")
    int revokeAllActiveByUserId(@Param("userId") int userId, @Param("now") Instant now);
}
