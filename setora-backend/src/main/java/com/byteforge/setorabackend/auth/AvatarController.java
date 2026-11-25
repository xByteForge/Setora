package com.byteforge.setorabackend.auth;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/avatar")
@RequiredArgsConstructor
public class AvatarController {

    private final AvatarService avatarService;

    @PostMapping("/upload")
    public ResponseEntity<@NonNull String> uploadAvatar(
            @RequestParam("file") MultipartFile file, @AuthenticationPrincipal Jwt jwt) {
        int userId = Integer.parseInt(jwt.getSubject()); // TODO When this is setup change accordingly
        avatarService.storeAvatar(file, userId);
        return ResponseEntity.ok("Upload successful");
    }

    @GetMapping("/{userId}")
    public ResponseEntity<byte[]> getAvatar(@PathVariable int userId) {
        String avatarFilename = avatarService.getAvatarPathByUser(userId);
        if (avatarFilename == null) {
            return ResponseEntity.notFound().build();
        }
        Path path = Paths.get("uploads/user-avatars", avatarFilename);

        if (!Files.exists(path)) {
            return ResponseEntity.notFound().build();
        }

        byte[] imageBytes;
        try {
            imageBytes = Files.readAllBytes(path);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        HttpHeaders headers = new HttpHeaders();
        MediaType type = MediaTypeFactory.getMediaType(avatarFilename).orElse(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentType(type);
        return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
    }
}
