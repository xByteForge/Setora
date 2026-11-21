package com.byteforge.setorabackend.serviceItem;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceItemRepository extends JpaRepository<ServiceItem, Integer> {
    List<ServiceItem> findByBusinessId(int businessId);
}

public interface ServiceItemRepository extends JpaRepository<ServiceItem, Integer> {}
