package com.byteforge.setorabackend.offering;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ServiceItemRepository extends JpaRepository<ServiceItem, UUID> {
}
