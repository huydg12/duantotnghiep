package com.poly.BE_main.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poly.BE_main.model.Employee;
import com.poly.BE_main.repository.EmployeeRepository;

@Service
public class EmployeeService {
    @Autowired
    EmployeeRepository employeeRepository;

    public List<Employee> findAll() {
        return employeeRepository.findAll();
    }

    public Employee create(Employee e) {
        employeeRepository.save(e);
        return e;
    }

    public void deleteById(Integer id) {
        employeeRepository.deleteById(id);
    }

    public Employee update(Employee e) {
        return employeeRepository.save(e);
    }

    public Employee update(int id, Employee eUpdate) {
        return employeeRepository.findById(id).map(e -> {
            e.setAccount(eUpdate.getAccount());
            e.setFullName(eUpdate.getFullName());
            e.setGender(eUpdate.getGender());
            e.setEmail(eUpdate.getEmail());
            e.setNumberPhone(eUpdate.getNumberPhone());
            e.setBirthOfDate(eUpdate.getBirthOfDate());
            e.setIsActive(eUpdate.getIsActive());
            e.setCreatedBy(eUpdate.getCreatedBy());
            e.setCreatedDate(eUpdate.getCreatedDate());
            return employeeRepository.save(e);
        }).orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên có id: " + id));
    }
}
