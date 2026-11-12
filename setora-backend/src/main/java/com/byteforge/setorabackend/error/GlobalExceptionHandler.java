package com.byteforge.setorabackend.error;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleInvalidParameter(
            EntityNotFoundException ex
    ) {
        return ResponseEntity
                .badRequest()
                .body(Map.of("message", "Entity not found:  " + ex.getMessage()));
    }
}
