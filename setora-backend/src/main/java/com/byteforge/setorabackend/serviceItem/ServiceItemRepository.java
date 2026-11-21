package com.byteforge.setorabackend.serviceItem;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ServiceItemRepository extends JpaRepository<ServiceItem, Integer> {
    List<ServiceItem> findByBusinessId(int businessId);
}
