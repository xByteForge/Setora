package com.byteforge.setorabackend.business;

import com.byteforge.setorabackend.business.dtos.BusinessFilterCriteria;
import com.byteforge.setorabackend.business.dtos.BusinessSortBy;
import com.byteforge.setorabackend.employees.model.UnavailableSlot;
import com.byteforge.setorabackend.employees.model.WorkSchedule;
import com.byteforge.setorabackend.serviceItem.ServiceItem;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public final class BusinessSpecification {

    private BusinessSpecification() {}

    public static Specification<Business> withCriteria(BusinessFilterCriteria criteria) {
        return (root, query, cb) -> {
            if (criteria == null) {
                return cb.conjunction();
            }

            List<Predicate> predicates = new ArrayList<>();

            if (criteria.ownerId() != null) {
                predicates.add(cb.equal(root.get("owner").get("id"), criteria.ownerId()));
            }

            if (criteria.location() != null && !criteria.location().isBlank()) {
                String pattern = containsPattern(criteria.location());
                Path<Location> loc = root.get("location");
                predicates.add(cb.or(
                        cb.like(cb.lower(loc.get("locationName")), pattern),
                        cb.like(cb.lower(loc.get("address")), pattern)
                ));
            }

            if (criteria.tags() != null && !criteria.tags().isEmpty()) {
                for (String tag : criteria.tags()) {
                    if (tag != null && !tag.isBlank()) {
                        Expression<Integer> pos = cb.function(
                                "array_position",
                                Integer.class,
                                root.get("tags"),
                                cb.literal(tag.trim())
                        );
                        predicates.add(cb.isNotNull(pos));
                    }
                }
            }

            if (criteria.hasService()) {
                predicates.add(hasMatchingService(root, query, cb, criteria));

                if (criteria.date() != null || criteria.time() != null) {
                    predicates.add(hasAvailableSchedule(root, query, cb, criteria));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Business> withPriceSort(BusinessFilterCriteria criteria, BusinessSortBy sortBy) {
        return (root, query, cb) -> {
            boolean isPriceSort = sortBy == BusinessSortBy.PRICE_ASC || sortBy == BusinessSortBy.PRICE_DESC;
            boolean isCountQuery = query.getResultType() == Long.class || query.getResultType() == long.class;

            if (!isPriceSort || isCountQuery) {
                return cb.conjunction();
            }

            Subquery<BigDecimal> priceSubquery = query.subquery(BigDecimal.class);
            Root<ServiceItem> serviceRoot = priceSubquery.from(ServiceItem.class);

            Predicate[] subPredicates = buildServicePredicates(root, serviceRoot, cb, criteria);
            Expression<BigDecimal> aggPrice = (sortBy == BusinessSortBy.PRICE_ASC)
                    ? cb.min(serviceRoot.get("price"))
                    : cb.max(serviceRoot.get("price"));

            priceSubquery.select(aggPrice).where(subPredicates);

            Order priceOrder = (sortBy == BusinessSortBy.PRICE_ASC)
                    ? cb.asc(priceSubquery)
                    : cb.desc(priceSubquery);

            query.orderBy(priceOrder, cb.asc(root.get("id")));
            return cb.conjunction();
        };
    }

    private static Predicate hasMatchingService(
            Root<Business> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            BusinessFilterCriteria criteria
    ) {
        Subquery<Integer> subquery = query.subquery(Integer.class);
        Root<ServiceItem> serviceRoot = subquery.from(ServiceItem.class);

        subquery.select(cb.literal(1))
                .where(buildServicePredicates(root, serviceRoot, cb, criteria));

        return cb.exists(subquery);
    }

    private static Predicate hasAvailableSchedule(
            Root<Business> root,
            CriteriaQuery<?> query,
            CriteriaBuilder cb,
            BusinessFilterCriteria criteria
    ) {
        Subquery<Integer> scheduleSubquery = query.subquery(Integer.class);
        Root<WorkSchedule> wsRoot = scheduleSubquery.from(WorkSchedule.class);

        List<Predicate> wsPredicates = new ArrayList<>();
        wsPredicates.add(cb.equal(wsRoot.get("employee").get("business"), root));

        if (criteria.date() != null) {
            wsPredicates.add(cb.equal(wsRoot.get("dayOfWeek"), criteria.date().getDayOfWeek()));
        }

        if (criteria.time() != null) {
            wsPredicates.add(coversTime(wsRoot.get("interval"), criteria.time(), cb));
        }

        if (criteria.date() != null && criteria.time() != null) {
            Subquery<Integer> unavailableSubquery = query.subquery(Integer.class);
            Root<UnavailableSlot> slotRoot = unavailableSubquery.from(UnavailableSlot.class);

            Predicate sameEmployee = cb.equal(slotRoot.get("employee"), wsRoot.get("employee"));
            Predicate matchesDateOrDay = cb.or(
                    cb.equal(slotRoot.get("specificDate"), criteria.date()),
                    cb.equal(slotRoot.get("dayOfWeek"), criteria.date().getDayOfWeek())
            );
            Predicate timeBlocked = coversTime(slotRoot.get("interval"), criteria.time(), cb);

            unavailableSubquery.select(cb.literal(1)).where(sameEmployee, matchesDateOrDay, timeBlocked);
            wsPredicates.add(cb.not(cb.exists(unavailableSubquery)));
        }

        scheduleSubquery.select(cb.literal(1)).where(wsPredicates.toArray(new Predicate[0]));
        return cb.exists(scheduleSubquery);
    }

    private static Predicate[] buildServicePredicates(
            Root<Business> businessRoot,
            Root<ServiceItem> serviceRoot,
            CriteriaBuilder cb,
            BusinessFilterCriteria criteria
    ) {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(serviceRoot.get("business"), businessRoot));
        predicates.add(cb.isTrue(serviceRoot.get("active")));

        if (criteria != null && criteria.hasService()) {
            predicates.add(cb.like(cb.lower(serviceRoot.get("name")), containsPattern(criteria.service())));

            if (criteria.minPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(serviceRoot.get("price"), criteria.minPrice()));
            }
            if (criteria.maxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(serviceRoot.get("price"), criteria.maxPrice()));
            }
        }

        return predicates.toArray(new Predicate[0]);
    }

    private static Predicate coversTime(Path<?> intervalPath, LocalTime time, CriteriaBuilder cb) {
        return cb.and(
                cb.lessThanOrEqualTo(intervalPath.get("startTime"), time),
                cb.greaterThan(intervalPath.get("endTime"), time)
        );
    }

    private static String containsPattern(String value) {
        return "%" + value.trim().toLowerCase() + "%";
    }
}