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
        String text,
        String location,
        List<String> tags,
        Integer categoryId,
        Integer serviceTypeId,
        @PositiveOrZero BigDecimal minPrice,
        @PositiveOrZero BigDecimal maxPrice,

        @FutureOrPresent @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        LocalDate date,

        @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime timeFrom,
        @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime timeTo) {
    @AssertTrue(message = "minPrice must be less than or equal to maxPrice")
    public boolean isPriceRangeValid() {
        if (minPrice == null || maxPrice == null) {
            return true;
        }
        return minPrice.compareTo(maxPrice) <= 0;
    }

    @AssertTrue(message = "timeFrom must be before or equal to timeTo")
    public boolean isTimeRangeValid() {
        if (timeFrom == null || timeTo == null) {
            return true;
        }
        return !timeFrom.isAfter(timeTo);
    }

    public boolean hasText() {
        return text != null && !text.isBlank();
    }

    public boolean hasService() {
        return serviceTypeId != null;
    }

    public boolean hasScheduleFilter() {
        return date != null || timeFrom != null || timeTo != null;
    }
}
