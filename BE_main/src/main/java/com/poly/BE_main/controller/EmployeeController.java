package com.poly.BE_main.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.poly.BE_main.model.Employee;
import com.poly.BE_main.service.EmployeeService;
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/employee")
public class EmployeeController {
@Autowired
EmployeeService employeeService;

@GetMapping("/show")
public List<Employee>findAll(){
    return employeeService.findAll();
}

@PostMapping("/add")
public Employee add(@RequestBody Employee e){
    System.out.println("thêm thành công");
    return employeeService.create(e);
}

@DeleteMapping("/delete/{id}")
public void delete(@PathVariable Integer id){
    employeeService.deleteById(id);
}

@PutMapping("/update/{id}")
public Employee update(@PathVariable int id, @RequestBody Employee e){
    return employeeService.update(id, e);
}
}