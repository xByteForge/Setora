package com.byteforge.setorabackend.serviceItem;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ServiceItemRepository extends JpaRepository<ServiceItem, UUID> {
}
