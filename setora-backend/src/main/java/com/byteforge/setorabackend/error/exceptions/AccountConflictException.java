package com.byteforge.setorabackend.error.exceptions;

public class AccountConflictException extends RuntimeException {
    public AccountConflictException(String message) {
        super(message);
    }
}
