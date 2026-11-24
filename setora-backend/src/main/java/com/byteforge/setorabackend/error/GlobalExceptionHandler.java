package com.byteforge.setorabackend.error;

import com.byteforge.setorabackend.error.exceptions.InvalidStateException;
import com.byteforge.setorabackend.error.exceptions.PriceChangedException;
import com.byteforge.setorabackend.error.exceptions.SlotTakenException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.nio.file.AccessDeniedException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Helper method to avoid repeating response construction
    private ResponseEntity<ApiErrorResponse> error(
            HttpStatus status,
            String code,
            String message,
            String fieldErrors
    ) {
        return ResponseEntity.status(status).body(
                new ApiErrorResponse(code, message, fieldErrors)
        );
    }

    // ==========================================
    // CUSTOM BUSINESS EXCEPTIONS
    // ==========================================

    @ExceptionHandler(SlotTakenException.class)
    public ResponseEntity<ApiErrorResponse> handleSlotTaken(
            SlotTakenException ex) {
        return error(
                HttpStatus.CONFLICT,
                "SLOT_TAKEN",
                ex.getMessage(),
                ""
        );
    }

    @ExceptionHandler(PriceChangedException.class)
    public ResponseEntity<ApiErrorResponse> handlePriceChanged(
            PriceChangedException ex) {
        return error(
                HttpStatus.CONFLICT,
                "PRICE_CHANGED",
                ex.getMessage(),
                ""
        );
    }

    @ExceptionHandler(InvalidStateException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidState(
            InvalidStateException ex) {
        return error(
                HttpStatus.CONFLICT,
                "INVALID_STATE",
                ex.getMessage(),
                ""
        );
    }

    // ==========================================
    // STANDARD EXCEPTIONS
    // ==========================================

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleInvalidParameter(EntityNotFoundException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", "Entity not found:  " + ex.getMessage()));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            EntityNotFoundException ex) {
        return error(
                HttpStatus.NOT_FOUND,
                "NOT_FOUND",
                ex.getMessage(),
                ""
        );
    }



    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleForbidden(
            AccessDeniedException ex) {
        return error(
                HttpStatus.FORBIDDEN,
                "FORBIDDEN",
                "You are not allowed to perform this action.",
                ""
        );
    }

    // ==========================================
    // FALLBACK
    // ==========================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(
            Exception ex) {

        //log.error("Unexpected server error", ex);

        return error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "UNKNOWN",
                "An unexpected error occurred.",
                ""
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format("Invalid value '%s' for parameter '%s'", ex.getValue(), ex.getName());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", message));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String message = ex.getAllErrors().stream()
                .map(error -> error instanceof FieldError fieldError
                        ? fieldError.getField() + ": " + fieldError.getDefaultMessage()
                        : error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", message));
    }
}