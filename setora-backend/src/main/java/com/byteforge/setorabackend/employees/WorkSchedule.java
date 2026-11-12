package com.byteforge.setorabackend.employees;

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
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.DayOfWeek;
import java.util.UUID;

//working hours of an employee in a day of the week
@Entity
@Table(
        name = "work_schedules",
        uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "day_of_week"}),
        check = @CheckConstraint(constraint = "end_time > start_time")
)
@Getter
@Setter
public class WorkSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false)
    private DayOfWeek dayOfWeek;

    @Embedded
    private TimeInterval interval;
}
