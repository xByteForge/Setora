package com.byteforge.setorabackend.error;

import com.byteforge.setorabackend.error.exceptions.*;
import jakarta.persistence.EntityNotFoundException;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Helper method to avoid repeating response construction
    private ResponseEntity<ApiErrorResponse> error(HttpStatus status, String code, String message, String fieldErrors) {
        return ResponseEntity.status(status).body(new ApiErrorResponse(code, message, fieldErrors));
    }

    // ==========================================
    // CUSTOM BUSINESS EXCEPTIONS
    // ==========================================

    @ExceptionHandler(SlotTakenException.class)
    public ResponseEntity<ApiErrorResponse> handleSlotTaken(SlotTakenException ex) {
        return error(HttpStatus.CONFLICT, "SLOT_TAKEN", ex.getMessage(), "");
    }

    @ExceptionHandler(PriceChangedException.class)
    public ResponseEntity<ApiErrorResponse> handlePriceChanged(PriceChangedException ex) {
        return error(HttpStatus.CONFLICT, "PRICE_CHANGED", ex.getMessage(), "");
    }

    @ExceptionHandler(InvalidStateException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidState(InvalidStateException ex) {
        return error(HttpStatus.CONFLICT, "INVALID_STATE", ex.getMessage(), "");
    }

    @ExceptionHandler(InvalidAvatarException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidAvatarException(InvalidAvatarException ex) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_AVATAR", ex.getMessage(), "");
    }

    @ExceptionHandler(AvatarStorageException.class)
    public ResponseEntity<ApiErrorResponse> handleAvatarStorage(AvatarStorageException ex) {
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "AVATAR_STORAGE_PROBLEM", ex.getMessage(), "");
    }

    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidInput(InvalidInputException ex) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_INPUT", ex.getMessage(), "");
    }

    @ExceptionHandler(InvalidPasswordException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidPassword(InvalidPasswordException ex) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_PASSWORD", ex.getMessage(), "");
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        return error(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", ex.getMessage(), "");
    }

    @ExceptionHandler(AccountConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleAccountConflict(AccountConflictException ex) {
        return error(HttpStatus.CONFLICT, "ACCOUNT_CONFLICT", ex.getMessage(), "");
    }

    @ExceptionHandler(DataConflictException.class)
    public ResponseEntity<ApiErrorResponse> handleDataConflict(DataConflictException ex) {
        return error(HttpStatus.CONFLICT, "DATA_CONFLICT", ex.getMessage(), "");
    }

    // ==========================================
    // STANDARD EXCEPTIONS
    // ==========================================

    //    @ExceptionHandler(EntityNotFoundException.class)
    //    public ResponseEntity<Map<String, String>> handleInvalidParameter(EntityNotFoundException ex) {
    //        return ResponseEntity.badRequest().body(Map.of("message", "Entity not found:  " + ex.getMessage()));
    //    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(EntityNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage(), "");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    ResponseEntity<ApiErrorResponse> tooLarge(MaxUploadSizeExceededException e) {
        return error(HttpStatus.CONTENT_TOO_LARGE, "UPLOAD_SIZE_ERROR", "Image is too large, max size is 2MB", "");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleForbidden(AccessDeniedException ex) {
        return error(HttpStatus.FORBIDDEN, "FORBIDDEN", "You are not allowed to perform this action.", "");
    }

    // without this, the Exception fallback below would turn these into 500
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiErrorResponse> handleResponseStatus(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
        String message = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
        return error(status, status.name(), message, "");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableBody(HttpMessageNotReadableException ex) {
        return error(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST", "The request body is missing or malformed.", "");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = String.format("Invalid value '%s' for parameter '%s'", ex.getValue(), ex.getName());
        return error(HttpStatus.BAD_REQUEST, "TYPE_MISMATCH", message, "");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String fieldErrors = ex.getAllErrors().stream()
                .map(error -> error instanceof FieldError fieldError
                        ? fieldError.getField() + ": " + fieldError.getDefaultMessage()
                        : error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "The request contains invalid fields.", fieldErrors);
    }

    // ==========================================
    // FALLBACK
    // ==========================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception ex) {

        // log.error("Unexpected server error", ex);

        return error(HttpStatus.INTERNAL_SERVER_ERROR, "UNKNOWN", "An unexpected error occurred.", "");
    }
}
