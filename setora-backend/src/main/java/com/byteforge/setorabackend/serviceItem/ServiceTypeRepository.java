package com.byteforge.setorabackend.serviceItem;

import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceTypeRepository extends JpaRepository<ServiceType, Integer> {
    List<ServiceType> findByCategoryId(int categoryId, Sort sort);
}
