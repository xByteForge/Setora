package com.byteforge.setorabackend.appointment;

import com.byteforge.setorabackend.auth.User;
import com.byteforge.setorabackend.common.DateTimeInterval;
import com.byteforge.setorabackend.employees.model.Employee;
import com.byteforge.setorabackend.serviceItem.ServiceItem;
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
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "appointments", check = @CheckConstraint(constraint = "ends_at > starts_at and price_at_appointment >= 0"))
@Getter
@Setter
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    //the business is the one of the service
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceItem serviceItem;

    @Embedded
    private DateTimeInterval interval;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status = AppointmentStatus.PENDING;

    //the service price can change later
    @Column(name = "price_at_appointment", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAtAppointment;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod = PaymentMethod.Physical;

    @Column(nullable = false)
    private boolean isPaid;

    @Column(nullable = true)
    private String clientNote;

}
