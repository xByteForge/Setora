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

@Entity
@Table(name = "appointments",
        check = {
                @CheckConstraint(
                        name = "chk_appointment_dates_price",
                        constraint = "ends_at > starts_at AND price_at_appointment >= 0"
                ),
                @CheckConstraint(
                        name = "chk_appointment_client",
                        constraint = """
                (
                    client_id IS NOT NULL
                    AND client_name IS NULL
                    AND client_email IS NULL
                    AND client_phone IS NULL
                )
                OR
                (
                    client_id IS NULL
                    AND client_name IS NOT NULL
                    AND (client_email IS NOT NULL OR client_phone IS NOT NULL)
                )
                """
                )
        })
@Getter
@Setter
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = true) //modified this, for ghost clients
    private User client;

    //these are relevant only if client==null !!!Attention do not use them if client field is not null!
    private String clientName;
    private String clientEmail;
    private String clientPhone;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    //the business is the one of the service
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "service_id", nullable = false)
    private ServiceItem serviceItem;//through here, we know of business and location

    @Embedded
    private DateTimeInterval interval;//holds start and end time, instead of start time and duration - duration is included in ServiceItem

    //the service price can change later
    @Column(name = "price_at_appointment", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAtAppointment;
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;


    //more of a payment status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentStatus status = AppointmentStatus.UNAVAILABLE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod = PaymentMethod.PHYSICAL;

    @Column(nullable = true)
    private String clientNote;


}

