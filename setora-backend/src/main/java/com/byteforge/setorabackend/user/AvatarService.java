package com.byteforge.setorabackend.user;

import com.byteforge.setorabackend.error.exceptions.AvatarStorageException;
import com.byteforge.setorabackend.error.exceptions.InvalidAvatarException;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
class AvatarService {

    private static final long MAX_BYTES = 2L * 1024 * 1024;
    private static final long MAX_PIXELS = 4_000_000;
    private static final int OUTPUT_SIZE = 512;
    private static final Set<String> ALLOWED = Set.of("jpeg", "png");

    static {
        ImageIO.setUseCache(false);
    }

    private final UserService userService;
    private final Path uploadDir;

    AvatarService(
            UserService userService, @Value("${setora.avatar.upload-dir:uploads/user-avatars}") String uploadDir) {
        this.userService = userService;
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
    }

    public void storeAvatar(MultipartFile file, int userId) {
        BufferedImage image = readAndValidate(file);

        byte[] png;
        try {
            png = resizeAndEncodePng(image);
        } catch (IOException e) {
            throw new AvatarStorageException("Could not process avatar", e);
        }

        String filename = UUID.randomUUID() + ".png";
        Path target = resolveSafely(filename);
        writeAtomically(png, target);

        String oldFilename;
        try {
            oldFilename = userService.updateAvatarPathByUserId(userId, filename);
        } catch (RuntimeException e) {
            deleteQuietly(target); // DB failed, so the new file is useless
            throw e;
        }

        if (oldFilename != null) {
            Path old = tryResolve(oldFilename);
            if (old != null && !old.equals(target)) {
                deleteQuietly(old); // failure here is only logged
            }
        }
    }

    public String getAvatarPathByUser(int userId) {
        return userService.getAvatarPathByUser(userId);
    }

    // full path of a stored avatar, or null if the filename points outside the upload folder
    public Path resolveAvatarFile(String filename) {
        return tryResolve(filename);
    }

    // ---------- file helpers ----------

    private Path resolveSafely(String filename) {
        Path p = uploadDir.resolve(filename).normalize();
        if (!uploadDir.equals(p.getParent())) {
            throw new InvalidAvatarException("Invalid avatar path");
        }
        return p;
    }

    private Path tryResolve(String filename) {
        try {
            return resolveSafely(filename);
        } catch (InvalidAvatarException e) {
            log.error("Refusing to touch suspicious avatar path from DB: {}", filename);
            return null;
        }
    }

    private void writeAtomically(byte[] data, Path target) {
        Path tmp = target.resolveSibling(target.getFileName() + ".tmp");
        try {
            Files.createDirectories(uploadDir);
            Files.write(tmp, data, StandardOpenOption.CREATE_NEW);
            Files.move(tmp, target, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException ex) {
            deleteQuietly(tmp);
            throw new AvatarStorageException("Could not store avatar", ex);
        }
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (Exception e) {
            log.warn("Failed to delete avatar file {}; it may be orphaned", path, e);
        }
    }

    // ---------- image validation + sanitization ----------

    private BufferedImage readAndValidate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidAvatarException("No file received");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new InvalidAvatarException("Image is too large, max size is 2MB");
        }

        try (InputStream in = file.getInputStream();
                ImageInputStream iis = ImageIO.createImageInputStream(in)) {

            if (iis == null) throw new InvalidAvatarException("Invalid image file");

            Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
            if (!readers.hasNext()) throw new InvalidAvatarException("Unsupported or invalid image format");

            ImageReader reader = readers.next();
            try {
                String format = reader.getFormatName().toLowerCase();
                if (!ALLOWED.contains(format)) {
                    throw new InvalidAvatarException("Only jpeg or png are accepted");
                }

                reader.setInput(iis, true, true);
                long w = reader.getWidth(0);
                long h = reader.getHeight(0);
                if (w <= 0 || h <= 0 || w * h > MAX_PIXELS) {
                    throw new InvalidAvatarException("Image dimensions are too large");
                }

                BufferedImage decoded = reader.read(0);
                if (decoded == null) throw new InvalidAvatarException("Invalid image file");

                return decoded;
            } finally {
                reader.dispose();
            }
        } catch (InvalidAvatarException ex) {
            throw ex; // our own messages pass through untouched
        } catch (IOException | RuntimeException ex) {
            throw new InvalidAvatarException("Could not read uploaded image");
        }
    }

    /** Fresh pixels only: drops EXIF/ICC/text chunks and any appended payload. */
    private byte[] resizeAndEncodePng(BufferedImage src) throws IOException {
        double scale = Math.min(1.0, (double) OUTPUT_SIZE / Math.max(src.getWidth(), src.getHeight()));
        int w = Math.max(1, (int) Math.round(src.getWidth() * scale));
        int h = Math.max(1, (int) Math.round(src.getHeight() * scale));

        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.drawImage(src, 0, 0, w, h, null);
        } finally {
            g.dispose();
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        if (!ImageIO.write(out, "png", bos)) throw new IOException("No PNG writer available");
        return bos.toByteArray();
    }
}
