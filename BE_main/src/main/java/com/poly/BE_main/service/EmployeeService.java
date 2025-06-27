package com.poly.BE_main.service;

import java.util.List;
import com.poly.BE_main.model.Employee;

public interface EmployeeService {
    List<Employee> findAll();
    Employee findById(Long id);
    Employee save(Employee employee);
    void deleteById(Long id);
}
