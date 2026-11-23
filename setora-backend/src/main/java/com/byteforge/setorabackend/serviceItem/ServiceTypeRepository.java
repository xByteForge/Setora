package com.byteforge.setorabackend.serviceItem;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceTypeRepository extends JpaRepository<ServiceType, Integer> {
    List<ServiceType> findByCategoryId(int categoryId, Sort sort);
}