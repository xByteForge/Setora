package com.byteforge.setorabackend.employees.repository;

import com.byteforge.setorabackend.employees.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    List<Employee> findByBusinessId(int businessId);
}
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {}
