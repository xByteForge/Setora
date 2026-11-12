package com.byteforge.setorabackend.serviceItem;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ServiceItemRepository extends JpaRepository<ServiceItem, UUID> {
    List<ServiceItem> findByBusinessId(UUID businessId);

    List<ServiceItem> findByBusinessIdAndActiveTrue(UUID businessId);

    @Query("""
        SELECT s FROM ServiceItem s
        WHERE s.business.id = :businessId
          AND s.active = true
          AND (:name IS NULL OR :name = '' OR LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%')))
        ORDER BY s.name ASC
    """)
    List<ServiceItem> searchServices(
            @Param("businessId") UUID businessId,
            @Param("name") String name
    );
}
