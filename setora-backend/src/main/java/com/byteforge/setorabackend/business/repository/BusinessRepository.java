package com.byteforge.setorabackend.business.repository;

import com.byteforge.setorabackend.business.model.Business;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BusinessRepository extends JpaRepository<Business, Integer>, JpaSpecificationExecutor<Business> {}
