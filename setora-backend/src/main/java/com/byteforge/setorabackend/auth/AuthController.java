package com.byteforge.setorabackend.auth;

import com.byteforge.setorabackend.auth.dtos.*;
import com.byteforge.setorabackend.user.UserService;
import com.byteforge.setorabackend.user.dtos.CreateUserRequestDto;
import com.byteforge.setorabackend.user.dtos.UpdateUserRequestDto;
import com.byteforge.setorabackend.user.dtos.UserResponseDto;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(@Valid @RequestBody CreateUserRequestDto request) {
        UserResponseDto response = authService.register(request);

        return ResponseEntity.created(URI.create("/api/users/" + response.id())).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDto> refresh(@Valid @RequestBody RefreshTokenRequestDto request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody RefreshTokenRequestDto request) {
        int userId = Integer.parseInt(jwt.getSubject());
        authService.logout(userId, request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        int userId = Integer.parseInt(jwt.getSubject());
        return ResponseEntity.ok(authService.getCurrentUser(userId));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponseDto> updateCurrentUser(
            @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UpdateUserRequestDto request) {
        int userId = Integer.parseInt(jwt.getSubject());

        return ResponseEntity.ok(userService.updateUser(userId, request));
    }

    @PutMapping("/change-password") // these are placeholders, we'll change them when implementing the password change
    public ResponseEntity<Void> changePassword() {
        return ResponseEntity.status(501).build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword() {
        return ResponseEntity.status(501).build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword() {
        return ResponseEntity.status(501).build();
    }
}
