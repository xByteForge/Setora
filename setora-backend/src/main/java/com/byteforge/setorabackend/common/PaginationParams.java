package com.byteforge.setorabackend.common;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record PaginationParams(
        @Min(0) Integer page, @Min(1) @Max(50) Integer size) {
    public PaginationParams {
        page = (page == null) ? 0 : page;
        size = (size == null) ? 20 : size;
    }
}
