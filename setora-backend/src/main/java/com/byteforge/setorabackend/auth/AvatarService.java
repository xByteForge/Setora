package com.byteforge.setorabackend.auth;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
class AvatarService {
    private final UserService userService;

    static Path uploadDir =
            Paths.get("uploads", "user-avatars").toAbsolutePath().normalize();

    public void storeAvatar(MultipartFile file, int userId) {
        validateImage(file);

        String ext =
                switch (Objects.requireNonNullElse(file.getContentType(), "")) {
                    case "image/png" -> "png";
                    case "image/webp" -> "webp";
                    default -> "jpg";
                };

        String filename = UUID.randomUUID().toString() + "." + ext;

        Path fullPath = uploadDir.resolve(filename);

        try {
            Files.createDirectories(uploadDir);
            file.transferTo(fullPath.toFile());
        } catch (IOException ex) {
            throw new RuntimeException("Could not store avatar", ex);
        }

        String oldPath = userService.updateAvatarPathByUserId(userId, filename);
        try {
            deleteOldAvatarPath(oldPath, filename);
        } catch (IOException e) {
            throw new RuntimeException("Error deleting old avatar", e);
        }
    }

    public String getAvatarPathByUser(int userId) {
        return userService.getAvatarPathByUser(userId);
    }

    // --------- helpers -------------
    private void validateImage(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("No file received");
        }

        if (file.getSize() > 2 * 1024 * 1024) {
            throw new IllegalArgumentException("Image is too large");
        }

        String contentType = file.getContentType();
        if (!List.of("image/jpeg", "image/png", "image/webp").contains(contentType)) {
            throw new IllegalArgumentException("Unsupported image type");
        }

        try (InputStream in = file.getInputStream()) {
            BufferedImage image = ImageIO.read(in);
            if (image == null) {
                throw new IllegalArgumentException("File is not a valid image");
            }
        } catch (IOException ex) {
            throw new IllegalStateException("Could not read uploaded file");
        }
    }

    private void deleteOldAvatarPath(String oldAvatarPath, String newAvatarPath) throws IOException {
        if (oldAvatarPath != null && !oldAvatarPath.equals(newAvatarPath)) {
            Files.deleteIfExists(uploadDir.resolve(oldAvatarPath));
        }
    }
}
