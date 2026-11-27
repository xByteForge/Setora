package com.byteforge.setorabackend.user;

import com.byteforge.setorabackend.auth.refresh_tokens.RefreshTokenRepository;
import com.byteforge.setorabackend.error.exceptions.DataConflictException;
import com.byteforge.setorabackend.error.exceptions.InvalidInputException;
import com.byteforge.setorabackend.error.exceptions.InvalidPasswordException;
import com.byteforge.setorabackend.user.dtos.CreateUserRequestDto;
import com.byteforge.setorabackend.user.dtos.UpdateUserRequestDto;
import com.byteforge.setorabackend.user.dtos.UserResponseDto;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    public UserResponseDto toResponseDto(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getUsername(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole(),
                user.isDeleted());
    }

    public static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public static void validatePassword(String password) {
        if (password == null || password.isBlank() || password.length() < 8)
            throw new InvalidPasswordException("Password must contain at least 8 characters");
        if (!(password.matches(".*[a-z].*") && password.matches(".*[A-Z].*") && password.matches(".*[^a-zA-Z0-9].*")))
            throw new InvalidPasswordException(
                    "Password must contain at least a small letter, a capital one and a special symbol.");
    }

    // if no username was given, the first name is used instead
    private static String resolveUsername(String username, String firstName) {
        return username == null || username.isBlank() ? firstName : username.trim();
    }

    private static String trimOrNull(String value) {
        return value == null ? null : value.trim();
    }

    // copies the data of a create/register request onto the user (password is hashed here)
    public void applyCreateData(User user, CreateUserRequestDto request) {
        String firstName = request.firstName().trim();

        user.setFirstName(firstName);
        user.setLastName(request.lastName().trim());
        user.setUsername(resolveUsername(request.username(), firstName));
        user.setEmail(normalizeEmail(request.email()));
        user.setPhoneNumber(trimOrNull(request.phoneNumber()));
        user.setPasswordHash(passwordEncoder.encode(request.password()));
    }

    public User saveOrConflict(User user, String conflictMessage) {
        try {
            return userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            throw new DataConflictException(conflictMessage, e);
        }
    }

    @Transactional
    public UserResponseDto createUser(CreateUserRequestDto request) {
        validatePassword(request.password());

        User user = new User();
        user.setRole(UserRole.EMPLOYEE);
        applyCreateData(user, request);

        return toResponseDto(saveOrConflict(user, "User information conflicts with database constraints"));
    }

    @Transactional
    public UserResponseDto updateUser(int id, UpdateUserRequestDto request) {
        User user = findActiveUserById(id);

        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setUsername(request.username().trim());
        user.setEmail(normalizeEmail(request.email()));
        user.setPhoneNumber(trimOrNull(request.phoneNumber()));

        return toResponseDto(saveOrConflict(user, "User information conflicts with database constraints"));
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> getUsers() {
        return userRepository.findAllActive().stream().map(this::toResponseDto).toList();
    }

    // finds all non-deleted users
    public User findActiveUserById(int id) {
        return userRepository
                .findById(id)
                .filter(user -> !user.isDeleted())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUserById(int id) {
        User user = findActiveUserById(id);
        return toResponseDto(user);
    }

    @Transactional
    public void deleteUser(int id) {
        User user = findActiveUserById(id);

        user.setDeleted(true);
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.saveAndFlush(user);

        refreshTokenRepository.revokeAllActiveByUserId(user.getId(), Instant.now());
    }

    public void updateClientDetails(User client, String lastName, String firstName, String email, String phoneNumber) {
        if (lastName != null) {
            if (lastName.isBlank()) throw new InvalidInputException("lastName cannot be blank");

            client.setLastName(lastName.trim());
        }

        if (firstName != null) {
            if (firstName.isBlank()) throw new InvalidInputException("firstName cannot be blank");

            client.setFirstName(firstName.trim());
        }

        if (email != null) {
            if (email.isBlank()) throw new InvalidInputException("Email cannot be blank");

            client.setEmail(normalizeEmail(email));
        }

        if (phoneNumber != null) client.setPhoneNumber(phoneNumber.trim());

        saveOrConflict(client, "Client details violate a database constraint");
    }

    @Transactional
    public String updateAvatarPathByUserId(int userId, String filename) {
        User user = userRepository.findById(userId).orElseThrow();
        String oldPath = user.getAvatarPath();
        user.setAvatarPath(filename);
        userRepository.save(user);
        return oldPath;
    }

    public String getAvatarPathByUser(int userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return user.getAvatarPath();
    }
}
