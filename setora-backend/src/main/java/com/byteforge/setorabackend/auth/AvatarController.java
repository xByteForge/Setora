package com.byteforge.setorabackend.auth;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;

@RestController
@RequestMapping("/api/avatar")
@RequiredArgsConstructor
public class AvatarController {

    private final AvatarService avatarService;


    @PostMapping("/upload")
    public ResponseEntity<@NonNull String> uploadAvatar(@RequestParam("file") MultipartFile file,
                                                 Principal principal) {
        int userId = 1;//principal.getId(); //TODO When this is setup change accordingly
        avatarService.storeAvatar(file, userId);
        return ResponseEntity.ok("Upload successful");
    }
}