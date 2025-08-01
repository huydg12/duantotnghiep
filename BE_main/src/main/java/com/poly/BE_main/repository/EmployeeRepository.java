package com.poly.BE_main.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.poly.BE_main.model.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    Optional<Employee> getEmployeeByAccountId(int accountId);
}