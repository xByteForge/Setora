package com.byteforge.setorabackend.error;

public record ApiErrorResponse(String code, String message, String fieldErrors) {
    public ApiErrorResponse {
        fieldErrors = fieldErrors == null ? "" : fieldErrors;
    }
}
