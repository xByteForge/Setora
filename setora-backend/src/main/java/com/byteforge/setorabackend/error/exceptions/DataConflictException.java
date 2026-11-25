package com.byteforge.setorabackend.error.exceptions;

public class DataConflictException extends RuntimeException {
    public DataConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
