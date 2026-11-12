package com.byteforge.setorabackend.employees.model;

import com.byteforge.setorabackend.common.TimeInterval;
import jakarta.persistence.CheckConstraint;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.UUID;

//period when an employee cannot take appointments (lunch break, blocked time, time off)
//either a one-off slot on a specific date or a recurring slot on a day of the week
//times are in the timezone of the business
@Entity
@Table(
        name = "unavailable_slots",
        check = @CheckConstraint(constraint = "(specific_date is null) <> (day_of_week is null) and end_time > start_time")
)
@Getter
@Setter
public class UnavailableSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UnavailableType type;

    @Column(columnDefinition = "text")
    private String reason;

    //set for a one-off slot, null for a recurring one
    @Column(name = "specific_date")
    private LocalDate specificDate;

    //set for a recurring weekly slot, null for a one-off one
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week")
    private DayOfWeek dayOfWeek;

    @Embedded
    private TimeInterval interval;

    public boolean isRecurring() {
        return dayOfWeek != null;
    }

    public boolean occursOn(LocalDate date) {
        if (specificDate != null) {
            return specificDate.equals(date);
        }
        return dayOfWeek == date.getDayOfWeek();
    }
}