package com.byteforge.setorabackend.business.dtos;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;

public record BusinessFilterCriteria(
        Integer ownerId,
        String location,
        List<String> tags,
        String category,
        String service,
        @PositiveOrZero BigDecimal minPrice,
        @PositiveOrZero BigDecimal maxPrice,
        @FutureOrPresent @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate date,
        @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time) {
    @AssertTrue(message = "minPrice must be less than or equal to maxPrice")
    public boolean isPriceRangeValid() {
        if (minPrice == null || maxPrice == null) {
            return true;
        }
        return minPrice.compareTo(maxPrice) <= 0;
    }

    public boolean hasService() {
        return service != null && !service.isBlank();
    }
}
