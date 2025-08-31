package com.poly.BE_main.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.poly.BE_main.dto.EmployeeCreateDTO;
import com.poly.BE_main.dto.EmployeeDTO;
import com.poly.BE_main.model.Account;
import com.poly.BE_main.model.Employee;
import com.poly.BE_main.service.AccountService;
import com.poly.BE_main.service.EmployeeService;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/employee")
public class EmployeeController {
    @Autowired
    EmployeeService employeeService;

    @Autowired
    AccountService accountService;

    @GetMapping("/show")
    public List<EmployeeDTO> findAll() {
        return employeeService.findAll();
    }

    @GetMapping("/showById/{id}")
    public ResponseEntity<EmployeeDTO> showById(@PathVariable Integer id) {
        return employeeService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/add")
    @Transactional
    public EmployeeDTO add(@RequestBody EmployeeCreateDTO dto) {
        // 1. Tạo account
        Account account = new Account();
        account.setUsername(dto.getUsername());
        account.setPassword(dto.getPassword() != null ? dto.getPassword() : "123456");
        account.setIsActive(true);
        account.setRoleId(dto.getRoleId() != null ? dto.getRoleId() : 1);
        account.setCreatedDate(LocalDateTime.now());
        account = accountService.createAccount(account);

        // 2. Tạo employee gắn account
        Employee employee = new Employee();
        employee.setFullName(dto.getFullName());
        employee.setGender(dto.isGender());
        employee.setEmail(dto.getEmail());
        employee.setNumberPhone(dto.getNumberPhone());
        employee.setBirthOfDate(dto.getBirthOfDate());
        employee.setActive(dto.isActive());
        employee.setCreatedBy(dto.getCreatedBy());
        employee.setCreatedDate(LocalDateTime.now());
        employee.setAccount(account);
        employee = accountService.createEmployee(employee);

        // 3. Trả về DTO hiển thị
        EmployeeDTO response = new EmployeeDTO();
        response.setId(employee.getId());
        response.setFullName(employee.getFullName());
        response.setGender(employee.isGender());
        response.setEmail(employee.getEmail());
        response.setNumberPhone(employee.getNumberPhone());
        response.setBirthOfDate(employee.getBirthOfDate());
        response.setActive(employee.isActive());
        response.setCreatedBy(employee.getCreatedBy());
        response.setCreatedDate(employee.getCreatedDate());
        response.setAccountId(account.getId());
        response.setAccountUsername(account.getUsername());

        return response;
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Integer id) {
        employeeService.deleteById(id);
    }

    @PutMapping("/update/{id}")
    public EmployeeDTO update(@PathVariable int id, @RequestBody EmployeeDTO e) {
        return employeeService.update(id, e);
    }

    @PutMapping("/updateStatus/{id}")
    public EmployeeDTO updateStatus(@PathVariable int id, @RequestBody EmployeeDTO e) {
        return employeeService.updateStatus(id, e);
    }
}