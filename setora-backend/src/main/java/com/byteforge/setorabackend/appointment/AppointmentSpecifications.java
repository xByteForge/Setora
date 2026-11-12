
package com.byteforge.setorabackend.appointment;

import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AppointmentSpecifications {

    public static Specification<Appointment> filterAppointments(
            UUID businessId,
            UUID employeeId,
            AppointmentStatus status,
            Instant rangeStart,
            Instant rangeEnd
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("serviceItem")
                                        .get("business")
                                        .get("id"),
                                        businessId));

            if (employeeId != null)
                predicates.add(cb.equal(root.get("employee").get("id"), employeeId));


            if (status != null)
                predicates.add(cb.equal(root.get("status"), status));


            predicates.add(cb.lessThan(root.get("interval").<Instant>get("start"), rangeEnd));

            predicates.add(cb.greaterThan(root.get("interval").<Instant>get("end"), rangeStart));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
