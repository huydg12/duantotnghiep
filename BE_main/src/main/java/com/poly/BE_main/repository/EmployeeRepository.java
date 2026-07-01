package com.poly.BE_main.repository;

import com.poly.BE_main.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    Optional<Employee> getEmployeeByAccountId(int accountId);
}