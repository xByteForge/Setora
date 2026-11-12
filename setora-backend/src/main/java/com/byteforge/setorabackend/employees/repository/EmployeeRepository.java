package com.byteforge.setorabackend.employees.repository;

import com.byteforge.setorabackend.employees.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
}
