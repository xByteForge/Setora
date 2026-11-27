package com.byteforge.setorabackend.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private UserService userService;

    private static final int USER_ID = 1;
    private static final String EMAIL = "maria.ionescu@gmail.com";
    private static final String PASSWORD = "Parola#123";
    private static final String PASSWORD_HASH = "hashed-password";

    @BeforeEach
    void setUp() {
        when(passwordEncoder.encode(any())).thenReturn(PASSWORD_HASH);
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private CreateUserRequestDto createRequest(String username) {
        return new CreateUserRequestDto(
                " Maria ", " Ionescu ", username, " Maria.Ionescu@Gmail.com ", " 0741111111 ", PASSWORD);
    }

    private User existingUser(int id, UserRole role, boolean deleted) {
        User user = new User("Maria", "Ionescu", EMAIL, "0741111111", PASSWORD_HASH);
        user.setId(id);
        user.setRole(role);
        user.setDeleted(deleted);
        return user;
    }

    // region createUser

    @Test
    void createUser_validRequest_savesEmployeeWithTrimmedData() {
        UserResponseDto result = userService.createUser(createRequest("  maria_i "));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).saveAndFlush(captor.capture());
        User saved = captor.getValue();

        assertEquals("Maria", saved.getFirstName());
        assertEquals("Ionescu", saved.getLastName());
        assertEquals("maria_i", saved.getUsername());
        assertEquals(EMAIL, saved.getEmail());
        assertEquals("0741111111", saved.getPhoneNumber());
        assertEquals(PASSWORD_HASH, saved.getPasswordHash());
        assertEquals(UserRole.EMPLOYEE, saved.getRole());
        assertFalse(saved.isDeleted());

        assertEquals(UserRole.EMPLOYEE, result.role());
        verify(passwordEncoder).encode(PASSWORD);
    }

    @Test
    void createUser_usernameMissing_usesFirstNameAsUsername() {
        UserResponseDto result = userService.createUser(createRequest(null));

        assertEquals("Maria", result.username());
    }

    @Test
    void createUser_usernameBlank_usesFirstNameAsUsername() {
        UserResponseDto result = userService.createUser(createRequest("    "));

        assertEquals("Maria", result.username());
    }

    @Test
    void createUser_phoneNumberMissing_savesNullPhoneNumber() {
        UserResponseDto result =
                userService.createUser(new CreateUserRequestDto("Maria", "Ionescu", null, EMAIL, null, PASSWORD));

        assertNull(result.phoneNumber());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Ab#1", "parola#123", "PAROLA#123", "Parola1234", "        "})
    void createUser_weakPassword_throwsInvalidPasswordAndSavesNothing(String password) {
        CreateUserRequestDto request = new CreateUserRequestDto("Maria", "Ionescu", null, EMAIL, null, password);

        assertThrows(InvalidPasswordException.class, () -> userService.createUser(request));

        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void createUser_databaseConstraintViolated_throwsDataConflict() {
        when(userRepository.saveAndFlush(any(User.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThrows(DataConflictException.class, () -> userService.createUser(createRequest(null)));
    }

    // endregion

    // region updateUser

    @Test
    void updateUser_activeUser_updatesTrimmedData() {
        User user = existingUser(USER_ID, UserRole.EMPLOYEE, false);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        UserResponseDto result = userService.updateUser(
                USER_ID, new UpdateUserRequestDto(" Ana ", " Pop ", " ana_p ", " Ana.Pop@Gmail.com ", null));

        assertEquals("Ana", result.firstName());
        assertEquals("Pop", result.lastName());
        assertEquals("ana_p", result.username());
        assertEquals("ana.pop@gmail.com", result.email());
        assertNull(result.phoneNumber());
        verify(userRepository).saveAndFlush(user);
    }

    @Test
    void updateUser_userMissing_throwsNotFound() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> userService.updateUser(USER_ID, new UpdateUserRequestDto("Ana", "Pop", "ana", EMAIL, null)));
    }

    @Test
    void updateUser_userDeleted_throwsNotFound() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser(USER_ID, UserRole.CLIENT, true)));

        assertThrows(
                EntityNotFoundException.class,
                () -> userService.updateUser(USER_ID, new UpdateUserRequestDto("Ana", "Pop", "ana", EMAIL, null)));

        verify(userRepository, never()).saveAndFlush(any());
    }

    @Test
    void updateUser_databaseConstraintViolated_throwsDataConflict() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser(USER_ID, UserRole.CLIENT, false)));
        when(userRepository.saveAndFlush(any(User.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThrows(
                DataConflictException.class,
                () -> userService.updateUser(USER_ID, new UpdateUserRequestDto("Ana", "Pop", "ana", EMAIL, null)));
    }

    // endregion

    // region find / get

    @Test
    void findByEmail_userExists_returnsUser() {
        User user = existingUser(USER_ID, UserRole.CLIENT, false);
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

        assertSame(user, userService.findByEmail(EMAIL));
    }

    @Test
    void findByEmail_userMissing_throwsNotFound() {
        when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.findByEmail(EMAIL));
    }

    @Test
    void findActiveUserById_activeUser_returnsUser() {
        User user = existingUser(USER_ID, UserRole.CLIENT, false);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        assertSame(user, userService.findActiveUserById(USER_ID));
    }

    @Test
    void findActiveUserById_deletedUser_throwsNotFound() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser(USER_ID, UserRole.CLIENT, true)));

        assertThrows(EntityNotFoundException.class, () -> userService.findActiveUserById(USER_ID));
    }

    @Test
    void getUserById_activeUser_returnsMappedDto() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser(USER_ID, UserRole.EMPLOYEE, false)));

        UserResponseDto result = userService.getUserById(USER_ID);

        assertEquals(
                new UserResponseDto(
                        USER_ID, "Maria", "Ionescu", "Maria", EMAIL, "0741111111", UserRole.EMPLOYEE, false),
                result);
    }

    @Test
    void getUsers_activeUsersExist_returnsAllMapped() {
        when(userRepository.findAllActive())
                .thenReturn(List.of(existingUser(1, UserRole.ADMIN, false), existingUser(2, UserRole.CLIENT, false)));

        List<UserResponseDto> result = userService.getUsers();

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).id());
        assertEquals(2, result.get(1).id());
    }

    @Test
    void getUsers_noActiveUsers_returnsEmptyList() {
        when(userRepository.findAllActive()).thenReturn(List.of());

        assertTrue(userService.getUsers().isEmpty());
    }

    // endregion

    // region deleteUser

    @Test
    void deleteUser_activeClient_marksDeletedAndRevokesTokens() {
        User user = existingUser(USER_ID, UserRole.CLIENT, false);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        userService.deleteUser(USER_ID);

        assertTrue(user.isDeleted());
        assertEquals(1, user.getTokenVersion());
        verify(userRepository).saveAndFlush(user);
        verify(refreshTokenRepository).revokeAllActiveByUserId(eq(USER_ID), any(Instant.class));
    }

    @Test
    void deleteUser_activeAdmin_marksDeletedAndRevokesTokens() {
        User admin = existingUser(USER_ID, UserRole.ADMIN, false);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(admin));

        userService.deleteUser(USER_ID);

        assertTrue(admin.isDeleted());
        verify(refreshTokenRepository).revokeAllActiveByUserId(eq(USER_ID), any(Instant.class));
    }

    @Test
    void deleteUser_userAlreadyDeleted_throwsNotFound() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(existingUser(USER_ID, UserRole.CLIENT, true)));

        assertThrows(EntityNotFoundException.class, () -> userService.deleteUser(USER_ID));

        verify(userRepository, never()).saveAndFlush(any());
        verify(refreshTokenRepository, never()).revokeAllActiveByUserId(eq(USER_ID), any());
    }

    // endregion

    // region updateClientDetails

    @Test
    void updateClientDetails_allValuesGiven_updatesTrimmedValues() {
        User client = existingUser(USER_ID, UserRole.CLIENT, false);

        userService.updateClientDetails(client, " Pop ", " Ana ", " Ana.Pop@Gmail.com ", " 0742222222 ");

        assertEquals("Pop", client.getLastName());
        assertEquals("Ana", client.getFirstName());
        assertEquals("ana.pop@gmail.com", client.getEmail());
        assertEquals("0742222222", client.getPhoneNumber());
        verify(userRepository).saveAndFlush(client);
    }

    @Test
    void updateClientDetails_allValuesNull_keepsOldValues() {
        User client = existingUser(USER_ID, UserRole.CLIENT, false);

        userService.updateClientDetails(client, null, null, null, null);

        assertEquals("Ionescu", client.getLastName());
        assertEquals("Maria", client.getFirstName());
        assertEquals(EMAIL, client.getEmail());
        assertEquals("0741111111", client.getPhoneNumber());
    }

    @Test
    void updateClientDetails_blankLastName_throwsInvalidInput() {
        User client = existingUser(USER_ID, UserRole.CLIENT, false);

        assertThrows(
                InvalidInputException.class, () -> userService.updateClientDetails(client, "  ", null, null, null));
    }

    @Test
    void updateClientDetails_blankFirstName_throwsInvalidInput() {
        User client = existingUser(USER_ID, UserRole.CLIENT, false);

        assertThrows(
                InvalidInputException.class, () -> userService.updateClientDetails(client, null, "  ", null, null));
    }

    @Test
    void updateClientDetails_blankEmail_throwsInvalidInput() {
        User client = existingUser(USER_ID, UserRole.CLIENT, false);

        assertThrows(
                InvalidInputException.class, () -> userService.updateClientDetails(client, null, null, "  ", null));
    }

    @Test
    void updateClientDetails_databaseConstraintViolated_throwsDataConflict() {
        User client = existingUser(USER_ID, UserRole.CLIENT, false);
        when(userRepository.saveAndFlush(any(User.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThrows(
                DataConflictException.class,
                () -> userService.updateClientDetails(client, null, null, "taken@gmail.com", null));
    }

    // endregion

    // region avatar

    @Test
    void updateAvatarPathByUserId_userExists_savesNewPathAndReturnsOldOne() {
        User user = existingUser(USER_ID, UserRole.CLIENT, false);
        user.setAvatarPath("old.png");
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        String oldPath = userService.updateAvatarPathByUserId(USER_ID, "new.png");

        assertEquals("old.png", oldPath);
        assertEquals("new.png", user.getAvatarPath());
        verify(userRepository).save(user);
    }

    @Test
    void getAvatarPathByUser_userExists_returnsPath() {
        User user = existingUser(USER_ID, UserRole.CLIENT, false);
        user.setAvatarPath("avatar.png");
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        assertEquals("avatar.png", userService.getAvatarPathByUser(USER_ID));
    }

    // endregion

    // region static helpers

    @Test
    void normalizeEmail_spacesAndCapitals_returnsTrimmedLowerCase() {
        assertEquals(EMAIL, UserService.normalizeEmail("  Maria.Ionescu@GMAIL.com "));
    }

    @Test
    void validatePassword_strongPassword_doesNotThrow() {
        assertDoesNotThrow(() -> UserService.validatePassword(PASSWORD));
    }

    @Test
    void validatePassword_nullPassword_throwsInvalidPassword() {
        assertThrows(InvalidPasswordException.class, () -> UserService.validatePassword(null));
    }

    // endregion
}
