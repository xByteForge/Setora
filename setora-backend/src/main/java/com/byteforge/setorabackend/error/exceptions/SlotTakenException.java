package com.byteforge.setorabackend.error.exceptions;

public class SlotTakenException extends RuntimeException {
    public SlotTakenException(String message) {
        super(message);
    }
}
