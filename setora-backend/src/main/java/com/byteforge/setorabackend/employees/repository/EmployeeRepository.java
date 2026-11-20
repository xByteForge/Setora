package com.byteforge.setorabackend.employees.repository;

import com.byteforge.setorabackend.employees.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {}
