package com.poly.BE_main.controller;

import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.poly.BE_main.model.Address;
import com.poly.BE_main.service.AddressService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/address")
public class AddressController {
    @Autowired
    AddressService addressService;

    @GetMapping("/show")
    public List<Address> findAll() {
        return addressService.findAll();
    }

    @PostMapping("/add")
    public Address add(@RequestBody Address a) {
        System.out.println("thêm thành công");
        return addressService.create(a);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Integer id) {
        addressService.deleteById(id);
    }

    @PutMapping("/update/{id}")
    public Address update(@PathVariable int id, @RequestBody Address a) {

        return addressService.update(id, a);
    }

    @GetMapping("/show/{customerId}")
    public List<Address> getByCustomerId(@PathVariable Integer customerId) {
        return addressService.findByCustomerId(customerId);
    }

}