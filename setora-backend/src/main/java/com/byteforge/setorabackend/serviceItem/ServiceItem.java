package com.byteforge.setorabackend.serviceItem;

import com.byteforge.setorabackend.appointment.PaymentMethod;
import com.byteforge.setorabackend.business.model.Business;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

// a service offered by a business
@Entity
@Table(name = "services", check = @CheckConstraint(constraint = "price >= 0 and duration_minutes > 0"))
@Getter
@Setter
public class ServiceItem {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private int id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(nullable = false, columnDefinition = "text")
    private String name;

    @Column(columnDefinition = "text")
    private String description; // more than name is needed

    @Column(nullable = false, precision = 10, scale = 2) // better precision than double
    private BigDecimal price;

    @Column(name = "duration_minutes", nullable = false)
    private int durationMinutes;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    // for services where payment depends and needs to be decided on site
    private PaymentMethod paymentMethod = PaymentMethod.PHYSICAL;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_type_id")
    private ServiceType serviceType;
}
