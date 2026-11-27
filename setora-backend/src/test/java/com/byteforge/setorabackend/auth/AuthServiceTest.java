package com.byteforge.setorabackend.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.byteforge.setorabackend.auth.dtos.AuthResponseDto;
import com.byteforge.setorabackend.auth.dtos.LoginRequestDto;
import com.byteforge.setorabackend.auth.dtos.RefreshTokenRequestDto;
import com.byteforge.setorabackend.auth.refresh_tokens.RefreshToken;
import com.byteforge.setorabackend.auth.refresh_tokens.RefreshTokenRepository;
import com.byteforge.setorabackend.error.exceptions.AccountConflictException;
import com.byteforge.setorabackend.error.exceptions.DataConflictException;
import com.byteforge.setorabackend.error.exceptions.InvalidCredentialsException;
import com.byteforge.setorabackend.error.exceptions.InvalidPasswordException;
import com.byteforge.setorabackend.user.User;
import com.byteforge.setorabackend.user.UserRepository;
import com.byteforge.setorabackend.user.UserRole;
import com.byteforge.setorabackend.user.UserService;
import com.byteforge.setorabackend.user.dtos.CreateUserRequestDto;
import com.byteforge.setorabackend.user.dtos.UserResponseDto;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.server.ResponseStatusException;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshJwtVerifier refreshJwtVerifier;

    private AuthService authService;

    private static final int USER_ID = 1;
    private static final String EMAIL = "ana.pop@gmail.com";
    private static final String PASSWORD = "Parola#123";
    private static final String PASSWORD_HASH = "hashed-password";

    private static final String OLD_TOKEN_ID = "old-token-id";
    private static final String NEW_TOKEN_ID = "new-token-id";
    private static final Instant NEW_EXPIRES_AT = Instant.parse("2030-01-01T00:00:00Z");
    private static final long ACCESS_LIFETIME = 900L;

    @BeforeEach
    void setUp() {
        // real UserService, so the shared user creation code is tested too
        UserService userService = new UserService(userRepository, passwordEncoder, refreshTokenRepository);
        authService = new AuthService(
                userRepository,
                userService,
                refreshTokenRepository,
                passwordEncoder,
                authenticationManager,
                jwtService,
                refreshJwtVerifier);

        when(passwordEncoder.encode(any())).thenReturn(PASSWORD_HASH);
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        when(jwtService.generateAccessToken(any(User.class))).thenReturn("access-token");
        when(jwtService.generateRefreshToken(any(User.class)))
                .thenReturn(new JwtService.IssuedRefreshToken("refresh-token", NEW_TOKEN_ID, NEW_EXPIRES_AT));
        when(jwtService.getAccessTokenLifetimeSeconds()).thenReturn(ACCESS_LIFETIME);
    }

    private CreateUserRequestDto registerRequest(String username) {
        return new CreateUserRequestDto("  Ana ", " Pop ", username, " Ana.Pop@Gmail.com ", " 0740000000 ", PASSWORD);
    }

    private User existingUser(UserRole role, boolean deleted) {
        User user = new User("Ana", "Pop", EMAIL, "0740000000", PASSWORD_HASH);
        user.setId(USER_ID);
        user.setRole(role);
        user.setDeleted(deleted);
        return user;
    }

    private Jwt refreshJwt(String subject, int tokenVersion) {
        return Jwt.withTokenValue("refresh-token-value")
                .header("alg", "HS256")
                .subject(subject)
                .jti(OLD_TOKEN_ID)
                .claim("token_version", tokenVersion)
                .build();
    }

    private RefreshToken session(User user, Instant expiresAt, Instant revokedAt) {
        RefreshToken session = new RefreshToken();
        session.setUser(user);
        session.setTokenId(OLD_TOKEN_ID);
        session.setExpiresAt(expiresAt);
        session.setRevokedAt(revokedAt);
        return session;
    }

    private void prepareRefreshSession(Jwt jwt, RefreshToken session) {
        when(refreshJwtVerifier.verify("refresh-token-value")).thenReturn(jwt);
        when(refreshTokenRepository.findByTokenIdForUpdate(OLD_TOKEN_ID)).thenReturn(Optional.of(session));
    }

    // only the refresh token errors are still ResponseStatusException
    private void assertStatus(HttpStatus expected, Runnable action) {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, action::run);
        assertEquals(expected, exception.getStatusCode());
    }

    // region register

    @Test
    void register_newUserWithUsername_savesClientWithTrimmedData() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        UserResponseDto result = authService.register(registerRequest("  ana_p  "));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(captor.capture());
        User saved = captor.getValue();

        assertEquals("Ana", saved.getFirstName());
        assertEquals("Pop", saved.getLastName());
        assertEquals("ana_p", saved.getUsername());
        assertEquals(EMAIL, saved.getEmail());
        assertEquals("0740000000", saved.getPhoneNumber());
        assertEquals(PASSWORD_HASH, saved.getPasswordHash());
        assertEquals(UserRole.CLIENT, saved.getRole());
        assertFalse(saved.isDeleted());

        assertEquals("ana_p", result.username());
        assertEquals(UserRole.CLIENT, result.role());
        verify(passwordEncoder).encode(PASSWORD);
    }

    @Test
    void register_usernameMissing_usesFirstNameAsUsername() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        UserResponseDto result = authService.register(registerRequest(null));

        assertEquals("Ana", result.username());
    }

    @Test
    void register_usernameBlank_usesFirstNameAsUsername() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        UserResponseDto result = authService.register(registerRequest("   "));

        assertEquals("Ana", result.username());
    }

    @Test
    void register_phoneNumberMissing_savesNullPhoneNumber() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        UserResponseDto result =
                authService.register(new CreateUserRequestDto("Ana", "Pop", null, EMAIL, null, PASSWORD));

        assertNull(result.phoneNumber());
    }

    @Test
    void register_emailWithSpacesAndCapitals_looksUpNormalizedEmail() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        authService.register(registerRequest(null));

        verify(userRepository).findByEmail(EMAIL);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Ab#1", "parola#123", "PAROLA#123", "Parola1234", "        "})
    void register_weakPassword_throwsInvalidPasswordAndSavesNothing(String password) {
        CreateUserRequestDto request = new CreateUserRequestDto("Ana", "Pop", null, EMAIL, null, password);

        assertThrows(InvalidPasswordException.class, () -> authService.register(request));

        verify(userRepository, never()).findByEmail(any());
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void register_activeAccountExists_throwsAccountConflict() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingUser(UserRole.CLIENT, false)));

        assertThrows(AccountConflictException.class, () -> authService.register(registerRequest(null)));

        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void register_deletedNonClientAccount_throwsAccountConflict() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingUser(UserRole.EMPLOYEE, true)));

        assertThrows(AccountConflictException.class, () -> authService.register(registerRequest(null)));

        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void register_deletedClientWrongPassword_throwsAccountConflict() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingUser(UserRole.CLIENT, true)));
        when(passwordEncoder.matches(PASSWORD, PASSWORD_HASH)).thenReturn(false);

        assertThrows(AccountConflictException.class, () -> authService.register(registerRequest(null)));

        verify(refreshTokenRepository, never()).revokeAllActiveByUserId(anyInt(), any());
        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void register_deletedClientCorrectPassword_restoresAccount() {
        User deletedClient = existingUser(UserRole.CLIENT, true);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(deletedClient));
        when(passwordEncoder.matches(PASSWORD, PASSWORD_HASH)).thenReturn(true);

        UserResponseDto result = authService.register(registerRequest("new_name"));

        assertFalse(deletedClient.isDeleted());
        assertEquals(1, deletedClient.getTokenVersion());
        assertEquals("new_name", deletedClient.getUsername());
        assertEquals(USER_ID, result.id());
        verify(refreshTokenRepository).revokeAllActiveByUserId(eq(USER_ID), any(Instant.class));
        verify(userRepository).saveAndFlush(deletedClient);
    }

    @Test
    void register_databaseConstraintViolated_throwsDataConflict() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());
        when(userRepository.saveAndFlush(any(User.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThrows(DataConflictException.class, () -> authService.register(registerRequest(null)));
    }

    // endregion

    // region login

    @Test
    void login_wrongCredentials_throwsInvalidCredentials() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        assertThrows(InvalidCredentialsException.class, () -> authService.login(new LoginRequestDto(EMAIL, PASSWORD)));

        verify(jwtService, never()).generateAccessToken(any());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void login_deletedUser_throwsInvalidCredentials() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingUser(UserRole.CLIENT, true)));

        assertThrows(InvalidCredentialsException.class, () -> authService.login(new LoginRequestDto(EMAIL, PASSWORD)));

        verify(jwtService, never()).generateAccessToken(any());
        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    void login_validCredentials_returnsTokensAndSavesSession() {
        User user = existingUser(UserRole.CLIENT, false);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        AuthResponseDto result = authService.login(new LoginRequestDto(EMAIL, PASSWORD));

        assertEquals("access-token", result.accessToken());
        assertEquals("refresh-token", result.refreshToken());
        assertEquals("Bearer", result.tokenType());
        assertEquals(ACCESS_LIFETIME, result.expiresInSeconds());
        assertEquals(USER_ID, result.user().id());

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        assertSame(user, captor.getValue().getUser());
        assertEquals(NEW_TOKEN_ID, captor.getValue().getTokenId());
        assertEquals(NEW_EXPIRES_AT, captor.getValue().getExpiresAt());
    }

    @Test
    void login_emailWithSpacesAndCapitals_authenticatesWithNormalizedEmail() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(existingUser(UserRole.CLIENT, false)));

        authService.login(new LoginRequestDto("  Ana.Pop@GMAIL.com ", PASSWORD));

        ArgumentCaptor<Authentication> captor = ArgumentCaptor.forClass(Authentication.class);
        verify(authenticationManager).authenticate(captor.capture());
        assertEquals(EMAIL, captor.getValue().getPrincipal());
        assertEquals(PASSWORD, captor.getValue().getCredentials());
    }

    // endregion

    // region refresh

    @Test
    void refresh_invalidToken_throwsUnauthorized() {
        when(refreshJwtVerifier.verify(any())).thenThrow(new JwtException("invalid"));

        assertStatus(
                HttpStatus.UNAUTHORIZED, () -> authService.refresh(new RefreshTokenRequestDto("refresh-token-value")));
    }

    @Test
    void refresh_subjectNotANumber_throwsUnauthorized() {
        when(refreshJwtVerifier.verify("refresh-token-value")).thenReturn(refreshJwt("abc", 0));

        assertStatus(
                HttpStatus.UNAUTHORIZED, () -> authService.refresh(new RefreshTokenRequestDto("refresh-token-value")));
    }

    @Test
    void refresh_sessionNotFound_throwsUnauthorized() {
        when(refreshJwtVerifier.verify("refresh-token-value")).thenReturn(refreshJwt("1", 0));
        when(refreshTokenRepository.findByTokenIdForUpdate(OLD_TOKEN_ID)).thenReturn(Optional.empty());

        assertStatus(
                HttpStatus.UNAUTHORIZED, () -> authService.refresh(new RefreshTokenRequestDto("refresh-token-value")));
    }

    @Test
    void refresh_sessionRevoked_throwsUnauthorized() {
        User user = existingUser(UserRole.CLIENT, false);
        prepareRefreshSession(refreshJwt("1", 0), session(user, Instant.now().plusSeconds(3600), Instant.now()));

        assertStatus(
                HttpStatus.UNAUTHORIZED, () -> authService.refresh(new RefreshTokenRequestDto("refresh-token-value")));
    }

    @Test
    void refresh_sessionExpired_throwsUnauthorized() {
        User user = existingUser(UserRole.CLIENT, false);
        prepareRefreshSession(refreshJwt("1", 0), session(user, Instant.now().minusSeconds(60), null));

        assertStatus(
                HttpStatus.UNAUTHORIZED, () -> authService.refresh(new RefreshTokenRequestDto("refresh-token-value")));
    }

    @Test
    void refresh_tokenVersionOutdated_throwsUnauthorized() {
        User user = existingUser(UserRole.CLIENT, false);
        user.setTokenVersion(2);
        prepareRefreshSession(refreshJwt("1", 1), session(user, Instant.now().plusSeconds(3600), null));

        assertStatus(
                HttpStatus.UNAUTHORIZED, () -> authService.refresh(new RefreshTokenRequestDto("refresh-token-value")));
    }

    @Test
    void refresh_userDeleted_throwsUnauthorized() {
        User user = existingUser(UserRole.CLIENT, true);
        prepareRefreshSession(refreshJwt("1", 0), session(user, Instant.now().plusSeconds(3600), null));

        assertStatus(
                HttpStatus.UNAUTHORIZED, () -> authService.refresh(new RefreshTokenRequestDto("refresh-token-value")));
    }

    @Test
    void refresh_sessionBelongsToAnotherUser_throwsUnauthorized() {
        User user = existingUser(UserRole.CLIENT, false);
        prepareRefreshSession(refreshJwt("2", 0), session(user, Instant.now().plusSeconds(3600), null));

        assertStatus(
                HttpStatus.UNAUTHORIZED, () -> authService.refresh(new RefreshTokenRequestDto("refresh-token-value")));
    }

    @Test
    void refresh_validSession_revokesOldSessionAndSavesNewOne() {
        User user = existingUser(UserRole.CLIENT, false);
        RefreshToken oldSession = session(user, Instant.now().plusSeconds(3600), null);
        prepareRefreshSession(refreshJwt("1", 0), oldSession);

        AuthResponseDto result = authService.refresh(new RefreshTokenRequestDto("refresh-token-value"));

        assertNotNull(oldSession.getRevokedAt());
        assertEquals("access-token", result.accessToken());
        assertEquals("refresh-token", result.refreshToken());
        assertEquals("Bearer", result.tokenType());

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        assertSame(user, captor.getValue().getUser());
        assertEquals(NEW_TOKEN_ID, captor.getValue().getTokenId());
        assertNull(captor.getValue().getRevokedAt());
    }

    // endregion

    // region logout

    @Test
    void logout_tokenOfAnotherUser_throwsUnauthorized() {
        when(refreshJwtVerifier.verify("refresh-token-value")).thenReturn(refreshJwt("2", 0));

        assertStatus(
                HttpStatus.UNAUTHORIZED,
                () -> authService.logout(USER_ID, new RefreshTokenRequestDto("refresh-token-value")));

        verify(refreshTokenRepository, never()).findByTokenIdForUpdate(any());
    }

    @Test
    void logout_sessionAlreadyRevoked_throwsUnauthorized() {
        User user = existingUser(UserRole.CLIENT, false);
        prepareRefreshSession(refreshJwt("1", 0), session(user, Instant.now().plusSeconds(3600), Instant.now()));

        assertStatus(
                HttpStatus.UNAUTHORIZED,
                () -> authService.logout(USER_ID, new RefreshTokenRequestDto("refresh-token-value")));
    }

    @Test
    void logout_validSession_revokesSession() {
        User user = existingUser(UserRole.CLIENT, false);
        RefreshToken session = session(user, Instant.now().plusSeconds(3600), null);
        prepareRefreshSession(refreshJwt("1", 0), session);

        authService.logout(USER_ID, new RefreshTokenRequestDto("refresh-token-value"));

        assertNotNull(session.getRevokedAt());
    }

    // endregion

    // region getCurrentUser

    @Test
    void getCurrentUser_activeUser_returnsUser() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser(UserRole.CLIENT, false)));

        UserResponseDto result = authService.getCurrentUser(USER_ID);

        assertEquals(USER_ID, result.id());
        assertEquals(EMAIL, result.email());
    }

    @Test
    void getCurrentUser_deletedUser_throwsNotFound() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser(UserRole.CLIENT, true)));

        assertThrows(EntityNotFoundException.class, () -> authService.getCurrentUser(USER_ID));
    }

    // endregion
}
