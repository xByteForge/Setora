package com.byteforge.setorabackend.common;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Embeddable
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DateTimeInterval {
    @Column(name = "starts_at", nullable = false)
    private Instant start;

    @Column(name = "ends_at", nullable = false)
    private Instant end;

    public DateTimeInterval(Instant start, Instant end) {
        if (start == null || end == null || !start.isBefore(end)) {
            throw new IllegalArgumentException("Start must be before end");
        }
        this.start = start;
        this.end = end;
    }

    public boolean overlaps(DateTimeInterval other) {
        return start.isBefore(other.end) && other.start.isBefore(end);
    }
}
