package com.byteforge.setorabackend.auth;

import com.byteforge.setorabackend.auth.dtos.CreateUserRequestDto;
import com.byteforge.setorabackend.auth.dtos.UpdateUserRequestDto;
import com.byteforge.setorabackend.auth.dtos.UserResponseDto;
import com.byteforge.setorabackend.auth.refresh_tokens.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));
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
                user.isDeleted()
        );
    }


    @Transactional
    public UserResponseDto createUser(CreateUserRequestDto request) {
        if (request.password().length() > 72) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Password too long"
            );
        }

        User user = new User();

        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setUsername(request.firstName().trim());
        user.setEmail(request.email());
        user.setPhoneNumber(request.phoneNumber() == null ? null : request.phoneNumber().trim());

        user.setPasswordHash(passwordEncoder.encode(request.password()));

        user.setRole(UserRole.EMPLOYEE);
        user.setDeleted(false);

        try {
            User savedUser = userRepository.saveAndFlush(user);
            return toResponseDto(savedUser);

        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "User information conflicts with database constraints",
                    e
            );
        }
    }

    @Transactional
    public UserResponseDto updateUser(int id, UpdateUserRequestDto request) {
        User user = findActiveUserById(id);

        if ("ghost@gmail.com".equalsIgnoreCase(user.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "The guest account cannot be modified"
            );
        }

        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setUsername(request.username().trim());
        user.setEmail(request.email());
        user.setPhoneNumber(request.phoneNumber() == null ? null : request.phoneNumber().trim());

        try {
            User savedUser = userRepository.saveAndFlush(user);
            return toResponseDto(savedUser);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "User information conflicts with database constraints",
                    e
            );
        }
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> getUsers() {
        return userRepository.findAllActive()
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    // finds all non-deleted users
    private User findActiveUserById(int id) {
        return userRepository.findById(id).filter(user -> !user.isDeleted())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));
    }

    @Transactional(readOnly = true)
    public UserResponseDto getUserById(int id) {
        User user = findActiveUserById(id);
        return toResponseDto(user);
    }


    @Transactional
    public void deleteUser(int id) {
        User user = findActiveUserById(id);

        if ("ghost@gmail.com".equalsIgnoreCase(user.getEmail())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "The guest account cannot be deleted"
            );
        }

        if (user.getRole() == UserRole.ADMIN && userRepository.countActiveByRole(UserRole.ADMIN) <= 1) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete the last existing administrator"
            );
        }

        user.setDeleted(true);
        user.setTokenVersion(user.getTokenVersion() + 1);
        userRepository.saveAndFlush(user);

        refreshTokenRepository.revokeAllActiveByUserId(user.getId(), Instant.now());
    }


    public void updateClientDetails(
            User client,
            String lastName,
            String firstName,
            String email,
            String phoneNumber
    ) {
        if (lastName != null) {
            if (lastName.isBlank())
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "lastName cannot be blank"
                );

            client.setLastName(lastName.trim());
        }

        if (firstName != null) {
            if (firstName.isBlank())
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "firstName cannot be blank"
                );

            client.setFirstName(firstName.trim());
        }

        if (email != null) {
            if (email.isBlank())
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Email cannot be blank"
                );

            client.setEmail(email);
        }

        if (phoneNumber != null)
            client.setPhoneNumber(phoneNumber.trim());

        try {
            userRepository.saveAndFlush(client);
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Client details violate a database constraint",
                    e
            );
        }
    }

    public String updateAvatarPathByUserId(int userId, String filename) {
        User user = userRepository.findById(userId).orElseThrow();
        String oldPath = user.getAvatarPath();
        user.setAvatarPath(filename);//TODO maybe you can get user directly in controller
        userRepository.save(user);
        return oldPath;
    }
    public String getAvatarPathByUser(int userId){
        User user = userRepository.findById(userId).orElseThrow();
        return user.getAvatarPath();
    }
}