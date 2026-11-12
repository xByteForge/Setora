
package com.byteforge.setorabackend.appointment;

import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AppointmentSpecifications {

    public static Specification<Appointment> filterAppointments(
            Integer businessId,
            Integer clientId,
            Integer serviceId,
            AppointmentStatus status,
            Instant rangeStart,
            Instant rangeEnd
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (businessId != null) {
                predicates.add(
                        cb.equal(
                                root.get("serviceItem")
                                        .get("business")
                                        .get("id"),
                                businessId
                        )
                );
            }

            if (clientId != null) {
                predicates.add(
                        cb.equal(root.get("client").get("id"), clientId)
                );
            }

            if (serviceId != null) {
                predicates.add(
                        cb.equal(root.get("serviceItem").get("id"), serviceId)
                );
            }

            if (status != null) {
                predicates.add(
                        cb.equal(root.get("status"), status)
                );
            }

            if (rangeStart != null && rangeEnd != null) {
                predicates.add(
                        cb.lessThan(
                                root.get("interval").<Instant>get("start"),
                                rangeEnd
                        )
                );

                predicates.add(
                        cb.greaterThan(
                                root.get("interval").<Instant>get("end"),
                                rangeStart
                        )
                );
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

