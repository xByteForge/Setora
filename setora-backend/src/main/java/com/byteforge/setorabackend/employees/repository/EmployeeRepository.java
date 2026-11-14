package com.byteforge.setorabackend.employees.repository;

import com.byteforge.setorabackend.employees.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    List<Employee> findByBusinessId(UUID businessId);

    List<Employee> findByBusinessIdAndActiveTrue(UUID businessId);
}
