package com.poly.BE_main.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.poly.BE_main.model.Customer;
import com.poly.BE_main.service.CustomerService;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/customer")
public class CustomerController {
    @Autowired
    CustomerService customerService;

    @GetMapping("/show")
    public List<Customer> findAll() {
        return customerService.findAll();
    }

    @PostMapping("/add")
    public Customer add(@RequestBody Customer c) {
        System.out.println("thêm thành công");
        return customerService.create(c);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Integer id) {
        customerService.deleteById(id);
    }

    @PutMapping("/update/{id}")
    public Customer update(@PathVariable int id, @RequestBody Customer c) {
        return customerService.update(id, c);
    }
}