package com.byteforge.setorabackend.business.repository;

import com.byteforge.setorabackend.business.model.Business;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BusinessRepository extends JpaRepository<Business, Integer>, JpaSpecificationExecutor<Business> {
    @Query("""
        SELECT DISTINCT b.location.locationName
        FROM Business b
        WHERE b.location.locationName IS NOT NULL
          AND TRIM(b.location.locationName) <> ''
        ORDER BY b.location.locationName ASC
    """)
    List<String> findDistinctLocations();
}
