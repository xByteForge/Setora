package com.byteforge.setorabackend.employees.repository;

import com.byteforge.setorabackend.employees.model.Employee;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    List<Employee> findAllByActiveTrueAndServiceItems_Id(int serviceId);
    List<Employee> findByBusinessId(int businessId);
}
