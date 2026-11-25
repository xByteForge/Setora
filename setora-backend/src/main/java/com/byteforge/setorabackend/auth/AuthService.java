package com.byteforge.setorabackend.auth;

import com.byteforge.setorabackend.auth.dtos.*;
import com.byteforge.setorabackend.auth.refresh_tokens.RefreshToken;
import com.byteforge.setorabackend.auth.refresh_tokens.RefreshTokenRepository;
import com.byteforge.setorabackend.error.exceptions.AccountConflictException;
import com.byteforge.setorabackend.error.exceptions.InvalidCredentialsException;
import com.byteforge.setorabackend.user.User;
import com.byteforge.setorabackend.user.UserRepository;
import com.byteforge.setorabackend.user.UserRole;
import com.byteforge.setorabackend.user.UserService;
import com.byteforge.setorabackend.user.dtos.CreateUserRequestDto;
import com.byteforge.setorabackend.user.dtos.UserResponseDto;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final UserService userService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshJwtVerifier refreshJwtVerifier;

    private ResponseStatusException invalidRefreshToken() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired refresh token");
    }

    private Jwt decodeRefreshToken(String token) {
        try {
            Jwt jwt = refreshJwtVerifier.verify(token);
            Integer.parseInt(jwt.getSubject());
            return jwt;

        } catch (JwtException | IllegalArgumentException e) {
            throw invalidRefreshToken();
        }
    }

    private User validateRefreshSession(Jwt jwt, RefreshToken session) {
        User user = session.getUser();
        int tokenUserId = Integer.parseInt(jwt.getSubject());
        Object versionClaim = jwt.getClaims().get("token_version");
        boolean validVersion =
                versionClaim instanceof Number && ((Number) versionClaim).intValue() == user.getTokenVersion();

        if (session.getRevokedAt() != null
                || !session.getExpiresAt().isAfter(Instant.now())
                || user.getId() != tokenUserId
                || user.isDeleted()
                || user.getRole() == null
                || !validVersion) throw invalidRefreshToken();

        return user;
    }

    @Transactional
    public AuthResponseDto refresh(RefreshTokenRequestDto request) {
        Jwt jwt = decodeRefreshToken(request.refreshToken());

        RefreshToken oldSession =
                refreshTokenRepository.findByTokenIdForUpdate(jwt.getId()).orElseThrow(this::invalidRefreshToken);

        User user = validateRefreshSession(jwt, oldSession);
        oldSession.setRevokedAt(Instant.now());

        String accessToken = jwtService.generateAccessToken(user);
        JwtService.IssuedRefreshToken newRefresh = jwtService.generateRefreshToken(user);

        RefreshToken newSession = new RefreshToken();
        newSession.setUser(user);
        newSession.setTokenId(newRefresh.tokenId());
        newSession.setExpiresAt(newRefresh.expiresAt());

        refreshTokenRepository.save(newSession);

        return new AuthResponseDto(
                accessToken,
                newRefresh.token(),
                "Bearer",
                jwtService.getAccessTokenLifetimeSeconds(),
                userService.toResponseDto(user));
    }

    @Transactional
    public UserResponseDto register(CreateUserRequestDto request) {
        UserService.validatePassword(request.password());
        String email = UserService.normalizeEmail(request.email());
        User user = userRepository.findByEmail(email).orElse(null);

        if (user != null) {
            if (!user.isDeleted()) throw new AccountConflictException("Account already exists");

            if (user.getRole() != UserRole.CLIENT) {
                throw new AccountConflictException("This account cannot be restored through registration");
            }

            if (!passwordEncoder.matches(request.password(), user.getPasswordHash()))
                throw new AccountConflictException("Account restoration requires the previous password");

            refreshTokenRepository.revokeAllActiveByUserId(user.getId(), Instant.now());
            user.setTokenVersion(user.getTokenVersion() + 1);
            user.setDeleted(false);

        } else {
            user = new User();
            user.setRole(UserRole.CLIENT);
        }

        userService.applyCreateData(user, request);

        return userService.toResponseDto(
                userService.saveOrConflict(user, "Account information conflicts with database constraints"));
    }

    @Transactional
    public AuthResponseDto login(LoginRequestDto request) {
        String email = UserService.normalizeEmail(request.email());

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));
        } catch (AuthenticationException e) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        // we're here if the account exists
        User user = userService.findByEmail(email);

        if (user.isDeleted()) throw new InvalidCredentialsException("Invalid email or password");

        String accessToken = jwtService.generateAccessToken(user);
        JwtService.IssuedRefreshToken refresh = jwtService.generateRefreshToken(user);

        RefreshToken session = new RefreshToken();
        session.setUser(user);
        session.setTokenId(refresh.tokenId());
        session.setExpiresAt(refresh.expiresAt());

        refreshTokenRepository.save(session);

        return new AuthResponseDto(
                accessToken,
                refresh.token(),
                "Bearer",
                jwtService.getAccessTokenLifetimeSeconds(),
                userService.toResponseDto(user));
    }

    @Transactional
    public void logout(int currentUserId, RefreshTokenRequestDto request) {
        Jwt jwt = decodeRefreshToken(request.refreshToken());

        if (Integer.parseInt(jwt.getSubject()) != currentUserId) throw invalidRefreshToken();

        RefreshToken session =
                refreshTokenRepository.findByTokenIdForUpdate(jwt.getId()).orElseThrow(this::invalidRefreshToken);
        validateRefreshSession(jwt, session);
        session.setRevokedAt(Instant.now());
    }

    @Transactional(readOnly = true)
    public UserResponseDto getCurrentUser(int userId) {
        return userService.getUserById(userId);
    }
}
