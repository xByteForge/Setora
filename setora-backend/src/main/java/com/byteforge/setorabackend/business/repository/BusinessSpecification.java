package com.byteforge.setorabackend.business.repository;

import com.byteforge.setorabackend.business.dtos.BusinessFilterCriteria;
import com.byteforge.setorabackend.business.dtos.BusinessSortBy;
import com.byteforge.setorabackend.business.model.Business;
import com.byteforge.setorabackend.employees.model.Employee;
import com.byteforge.setorabackend.employees.model.WorkSchedule;
import com.byteforge.setorabackend.serviceItem.ServiceItem;
import jakarta.persistence.criteria.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

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

            if (criteria.categoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), criteria.categoryId()));
            }

            if (criteria.hasText()) {
                predicates.add(hasMatchingText(root, query, cb, criteria.text()));
            }

            if (criteria.location() != null && !criteria.location().isBlank()) {
                Expression<String> pattern = toSearchPattern(cb, criteria.location());
                predicates.add(cb.or(
                        cb.like(
                                unaccentLower(
                                        cb, cb.coalesce(root.get("location").get("locationName"), "")),
                                pattern),
                        cb.like(
                                unaccentLower(
                                        cb, cb.coalesce(root.get("location").get("address"), "")),
                                pattern)));
            }

            if (criteria.tags() != null && !criteria.tags().isEmpty()) {
                for (String tag : criteria.tags()) {
                    if (tag != null && !tag.isBlank()) {
                        predicates.add(cb.isNotNull(cb.function(
                                "array_position", Integer.class, root.get("tags"), cb.literal(tag.trim()))));
                    }
                }
            }

            if (criteria.hasService() || criteria.minPrice() != null || criteria.maxPrice() != null) {
                predicates.add(hasMatchingService(root, query, cb, criteria));
            }

            if (criteria.hasScheduleFilter()) {
                predicates.add(hasAvailableSchedule(root, query, cb, criteria));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Business> withPriceSort(BusinessFilterCriteria criteria, BusinessSortBy sortBy) {
        return (root, query, cb) -> {
            if (sortBy != BusinessSortBy.PRICE_ASC && sortBy != BusinessSortBy.PRICE_DESC) {
                return cb.conjunction();
            }

            if (Long.class.equals(query.getResultType()) || long.class.equals(query.getResultType())) {
                return cb.conjunction();
            }

            boolean ascending = sortBy == BusinessSortBy.PRICE_ASC;

            Subquery<BigDecimal> priceSubquery = query.subquery(BigDecimal.class);
            Root<ServiceItem> serviceRoot = priceSubquery.from(ServiceItem.class);

            List<Predicate> servicePredicates = buildServicePredicates(root, serviceRoot, cb, criteria);
            priceSubquery.select(cb.min(serviceRoot.get("price"))).where(servicePredicates.toArray(new Predicate[0]));

            query.orderBy(ascending ? cb.asc(priceSubquery) : cb.desc(priceSubquery), cb.asc(root.get("id")));

            return cb.conjunction();
        };
    }

    private static Predicate hasMatchingText(
            Root<Business> root, CriteriaQuery<?> query, CriteriaBuilder cb, String rawText) {
        Expression<String> pattern = toSearchPattern(cb, rawText);

        Subquery<Integer> serviceSubquery = query.subquery(Integer.class);
        Root<ServiceItem> serviceRoot = serviceSubquery.from(ServiceItem.class);
        serviceSubquery
                .select(cb.literal(1))
                .where(
                        cb.equal(serviceRoot.get("business"), root),
                        cb.isTrue(serviceRoot.get("active")),
                        cb.or(
                                cb.like(unaccentLower(cb, serviceRoot.get("name")), pattern),
                                cb.like(
                                        unaccentLower(
                                                cb,
                                                serviceRoot.get("serviceType").get("name")),
                                        pattern)));

        Expression<String> tagsAsText = cb.function("array_to_string", String.class, root.get("tags"), cb.literal(" "));

        return cb.or(
                cb.like(unaccentLower(cb, root.get("name")), pattern),
                cb.like(unaccentLower(cb, cb.coalesce(root.get("description"), "")), pattern),
                cb.like(unaccentLower(cb, root.get("category").get("name")), pattern),
                cb.like(unaccentLower(cb, cb.coalesce(root.get("location").get("locationName"), "")), pattern),
                cb.like(unaccentLower(cb, cb.coalesce(root.get("location").get("address"), "")), pattern),
                cb.like(unaccentLower(cb, cb.coalesce(tagsAsText, "")), pattern),
                cb.exists(serviceSubquery));
    }

    private static Predicate hasMatchingService(
            Root<Business> root, CriteriaQuery<?> query, CriteriaBuilder cb, BusinessFilterCriteria criteria) {
        Subquery<Integer> serviceSubquery = query.subquery(Integer.class);
        Root<ServiceItem> serviceRoot = serviceSubquery.from(ServiceItem.class);

        List<Predicate> servicePredicates = buildServicePredicates(root, serviceRoot, cb, criteria);
        serviceSubquery.select(cb.literal(1)).where(servicePredicates.toArray(new Predicate[0]));

        return cb.exists(serviceSubquery);
    }

    private static List<Predicate> buildServicePredicates(
            Root<Business> businessRoot,
            Root<ServiceItem> serviceRoot,
            CriteriaBuilder cb,
            BusinessFilterCriteria criteria) {
        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(serviceRoot.get("business"), businessRoot));
        predicates.add(cb.isTrue(serviceRoot.get("active")));

        if (criteria != null) {
            if (criteria.hasService()) {
                predicates.add(cb.equal(serviceRoot.get("serviceType").get("id"), criteria.serviceTypeId()));
            }
            if (criteria.minPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(serviceRoot.get("price"), criteria.minPrice()));
            }
            if (criteria.maxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(serviceRoot.get("price"), criteria.maxPrice()));
            }
        }

        return predicates;
    }

    private static Predicate hasAvailableSchedule(
            Root<Business> root, CriteriaQuery<?> query, CriteriaBuilder cb, BusinessFilterCriteria criteria) {
        Subquery<Integer> scheduleSubquery = query.subquery(Integer.class);
        Root<WorkSchedule> scheduleRoot = scheduleSubquery.from(WorkSchedule.class);
        Join<WorkSchedule, Employee> employeeJoin = scheduleRoot.join("employee");

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(employeeJoin.get("business"), root));

        if (criteria.hasService()) {
            Join<Employee, ServiceItem> serviceJoin = employeeJoin.join("serviceItems");
            predicates.add(cb.isTrue(serviceJoin.get("active")));
            predicates.add(cb.equal(serviceJoin.get("serviceType").get("id"), criteria.serviceTypeId()));
        }

        if (criteria.date() != null) {
            predicates.add(
                    cb.equal(scheduleRoot.get("dayOfWeek"), criteria.date().getDayOfWeek()));
        }

        if (criteria.timeFrom() != null) {
            predicates.add(cb.greaterThan(scheduleRoot.get("interval").get("end"), criteria.timeFrom()));
        }

        if (criteria.timeTo() != null) {
            predicates.add(cb.lessThan(scheduleRoot.get("interval").get("start"), criteria.timeTo()));
        }

        scheduleSubquery.select(cb.literal(1)).where(predicates.toArray(new Predicate[0]));

        return cb.exists(scheduleSubquery);
    }

    private static Expression<String> toSearchPattern(CriteriaBuilder cb, String value) {
        return cb.concat(cb.concat("%", unaccentLower(cb, value.trim())), "%");
    }

    private static Expression<String> unaccentLower(CriteriaBuilder cb, Expression<String> expr) {
        return cb.function("unaccent", String.class, cb.lower(expr));
    }

    private static Expression<String> unaccentLower(CriteriaBuilder cb, String value) {
        return cb.function("unaccent", String.class, cb.lower(cb.literal(value)));
    }
}
