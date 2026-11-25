package com.byteforge.setorabackend.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.byteforge.setorabackend.error.exceptions.InvalidAvatarException;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockMultipartFile;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class AvatarServiceTest {

    private static final int USER_ID = 1;
    private static final byte[] PNG_SIGNATURE = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};

    @Mock
    private UserService userService;

    @TempDir
    Path tempDir;

    private Path uploadDir;
    private AvatarService avatarService;

    // AvatarService logs expected failures; they are captured here instead of printed to the console
    private final Logger serviceLogger = (Logger) LoggerFactory.getLogger(AvatarService.class);
    private final ListAppender<ILoggingEvent> serviceLogs = new ListAppender<>();

    @BeforeEach
    void setUp() {
        // not created up front, so the tests also check that the service creates it
        uploadDir = tempDir.resolve("user-avatars");
        avatarService = new AvatarService(userService, uploadDir.toString());

        serviceLogs.start();
        serviceLogger.addAppender(serviceLogs);
        serviceLogger.setAdditive(false);
    }

    @AfterEach
    void restoreLogging() {
        serviceLogger.detachAppender(serviceLogs);
        serviceLogger.setAdditive(true);
        serviceLogs.stop();
    }

    private Set<Path> filesInUploadDir() throws IOException {
        if (!Files.isDirectory(uploadDir)) {
            return Set.of();
        }
        try (Stream<Path> files = Files.list(uploadDir)) {
            return files.collect(Collectors.toSet());
        }
    }

    private static byte[] imageBytes(int width, int height, String format) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(Color.ORANGE);
            graphics.fillRect(0, 0, width, height);
        } finally {
            graphics.dispose();
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        assertTrue(ImageIO.write(image, format, out), "no writer for " + format);
        return out.toByteArray();
    }

    private static MockMultipartFile upload(byte[] content, String contentType) {
        return new MockMultipartFile("file", "avatar", contentType, content);
    }

    private String storedFilename() {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(userService).updateAvatarPathByUserId(eq(USER_ID), captor.capture());
        return captor.getValue();
    }

    private void assertInvalidAvatarAndNothingStored(MockMultipartFile file, String expectedMessage)
            throws IOException {
        InvalidAvatarException exception =
                assertThrows(InvalidAvatarException.class, () -> avatarService.storeAvatar(file, USER_ID));

        if (expectedMessage != null) {
            assertEquals(expectedMessage, exception.getMessage());
        }
        verify(userService, never()).updateAvatarPathByUserId(anyInt(), anyString());
        assertTrue(filesInUploadDir().isEmpty());
    }

    // region storeAvatar - valid images

    @Test
    void storeAvatar_largePng_storesPngResizedTo512AndSavesFilename() throws IOException {
        avatarService.storeAvatar(upload(imageBytes(1024, 512, "png"), "image/png"), USER_ID);

        String filename = storedFilename();
        Path stored = uploadDir.resolve(filename);

        assertTrue(filename.endsWith(".png"));
        assertTrue(Files.exists(stored));
        BufferedImage result = ImageIO.read(stored.toFile());
        assertEquals(512, result.getWidth());
        assertEquals(256, result.getHeight());
        assertEquals(Set.of(stored), filesInUploadDir()); // no .tmp file left behind
    }

    @Test
    void storeAvatar_smallJpeg_keepsSizeAndStoresAsPng() throws IOException {
        avatarService.storeAvatar(upload(imageBytes(100, 50, "jpeg"), "image/jpeg"), USER_ID);

        Path stored = uploadDir.resolve(storedFilename());
        byte[] content = Files.readAllBytes(stored);

        assertArrayEquals(PNG_SIGNATURE, Arrays.copyOf(content, PNG_SIGNATURE.length));
        BufferedImage result = ImageIO.read(new ByteArrayInputStream(content));
        assertEquals(100, result.getWidth());
        assertEquals(50, result.getHeight());
    }

    @Test
    void storeAvatar_userHadAvatar_deletesOldFile() throws IOException {
        Files.createDirectories(uploadDir);
        Path oldAvatar = Files.write(uploadDir.resolve(UUID.randomUUID() + ".png"), imageBytes(10, 10, "png"));
        when(userService.updateAvatarPathByUserId(eq(USER_ID), anyString()))
                .thenReturn(oldAvatar.getFileName().toString());

        avatarService.storeAvatar(upload(imageBytes(64, 64, "png"), "image/png"), USER_ID);

        assertFalse(Files.exists(oldAvatar));
        assertTrue(Files.exists(uploadDir.resolve(storedFilename())));
    }

    @Test
    void storeAvatar_oldFileCannotBeDeleted_keepsNewAvatar() throws IOException {
        // a non-empty folder with the old avatar's name cannot be deleted, so the cleanup fails
        Path undeletableOldAvatar = Files.createDirectories(uploadDir.resolve("old-avatar.png"));
        Files.writeString(undeletableOldAvatar.resolve("blocker.txt"), "not empty");
        when(userService.updateAvatarPathByUserId(eq(USER_ID), anyString())).thenReturn("old-avatar.png");

        assertDoesNotThrow(() -> avatarService.storeAvatar(upload(imageBytes(64, 64, "png"), "image/png"), USER_ID));

        Path newAvatar = uploadDir.resolve(storedFilename());
        assertTrue(Files.exists(newAvatar));
        assertEquals(64, ImageIO.read(newAvatar.toFile()).getWidth());
        assertTrue(Files.exists(undeletableOldAvatar));
        verify(userService, times(1)).updateAvatarPathByUserId(eq(USER_ID), anyString());

        // the failed cleanup is only logged as a warning
        assertEquals(1, serviceLogs.list.size());
        assertEquals(Level.WARN, serviceLogs.list.get(0).getLevel());
        assertTrue(serviceLogs.list.get(0).getFormattedMessage().contains("old-avatar.png"));
    }

    @Test
    void storeAvatar_oldPathOutsideUploadDir_doesNotDeleteIt() throws IOException {
        Path outsideFile = Files.writeString(tempDir.resolve("not-an-avatar.txt"), "keep me");
        when(userService.updateAvatarPathByUserId(eq(USER_ID), anyString()))
                .thenReturn(outsideFile.toAbsolutePath().toString());

        avatarService.storeAvatar(upload(imageBytes(64, 64, "png"), "image/png"), USER_ID);

        assertTrue(Files.exists(outsideFile));
        assertTrue(Files.exists(uploadDir.resolve(storedFilename())));
    }

    @Test
    void storeAvatar_databaseUpdateFails_deletesNewFileAndRethrows() throws IOException {
        RuntimeException failure = new RuntimeException("database down");
        when(userService.updateAvatarPathByUserId(eq(USER_ID), anyString())).thenThrow(failure);

        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> avatarService.storeAvatar(upload(imageBytes(64, 64, "png"), "image/png"), USER_ID));

        assertSame(failure, thrown);
        assertTrue(filesInUploadDir().isEmpty());
    }

    // endregion

    // region storeAvatar - rejected uploads

    @Test
    void storeAvatar_nullFile_throwsInvalidAvatar() throws IOException {
        InvalidAvatarException exception =
                assertThrows(InvalidAvatarException.class, () -> avatarService.storeAvatar(null, USER_ID));

        assertEquals("No file received", exception.getMessage());
        verify(userService, never()).updateAvatarPathByUserId(anyInt(), anyString());
        assertTrue(filesInUploadDir().isEmpty());
    }

    @Test
    void storeAvatar_emptyFile_throwsInvalidAvatar() throws IOException {
        assertInvalidAvatarAndNothingStored(upload(new byte[0], "image/png"), "No file received");
    }

    @Test
    void storeAvatar_fileOver2MB_throwsInvalidAvatar() throws IOException {
        byte[] tooLarge = new byte[2 * 1024 * 1024 + 1];

        assertInvalidAvatarAndNothingStored(upload(tooLarge, "image/png"), "Image is too large, max size is 2MB");
    }

    @Test
    void storeAvatar_notAnImage_throwsInvalidAvatar() throws IOException {
        byte[] text = "definitely not an image".getBytes(StandardCharsets.UTF_8);

        assertInvalidAvatarAndNothingStored(upload(text, "image/png"), "Unsupported or invalid image format");
    }

    @Test
    void storeAvatar_gifImage_throwsInvalidAvatar() throws IOException {
        assertInvalidAvatarAndNothingStored(
                upload(imageBytes(32, 32, "gif"), "image/gif"), "Only jpeg or png are accepted");
    }

    @Test
    void storeAvatar_tooManyPixels_throwsInvalidAvatar() throws IOException {
        // 2001 x 2000 is just over the 4 000 000 pixel limit, but small on disk
        assertInvalidAvatarAndNothingStored(
                upload(imageBytes(2001, 2000, "png"), "image/png"), "Image dimensions are too large");
    }

    @Test
    void storeAvatar_truncatedPng_throwsInvalidAvatar() throws IOException {
        byte[] truncated = Arrays.copyOf(imageBytes(64, 64, "png"), 40); // header only, no pixel data

        assertInvalidAvatarAndNothingStored(upload(truncated, "image/png"), null);
    }

    // endregion

    // region resolveAvatarFile

    @Test
    void resolveAvatarFile_plainFilename_returnsPathInsideUploadDir() {
        Path result = avatarService.resolveAvatarFile("avatar.png");

        assertEquals(uploadDir.toAbsolutePath().normalize().resolve("avatar.png"), result);
    }

    @Test
    void resolveAvatarFile_parentDirectoryTraversal_returnsNull() {
        assertNull(avatarService.resolveAvatarFile("../secret.txt"));
    }

    @Test
    void resolveAvatarFile_absolutePathOutsideUploadDir_returnsNull() {
        assertNull(avatarService.resolveAvatarFile(
                tempDir.resolve("other.png").toAbsolutePath().toString()));
    }

    @Test
    void resolveAvatarFile_subfolder_returnsNull() {
        assertNull(avatarService.resolveAvatarFile("nested/avatar.png"));
    }

    // endregion

    // region getAvatarPathByUser

    @Test
    void getAvatarPathByUser_userHasAvatar_returnsFilename() {
        when(userService.getAvatarPathByUser(USER_ID)).thenReturn("avatar.png");

        assertEquals("avatar.png", avatarService.getAvatarPathByUser(USER_ID));
        verify(userService).getAvatarPathByUser(USER_ID);
    }

    @Test
    void getAvatarPathByUser_userHasNoAvatar_returnsNull() {
        when(userService.getAvatarPathByUser(USER_ID)).thenReturn(null);

        assertNull(avatarService.getAvatarPathByUser(USER_ID));
    }

    // endregion
}
