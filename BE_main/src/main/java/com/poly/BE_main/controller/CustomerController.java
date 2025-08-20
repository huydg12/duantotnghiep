package com.poly.BE_main.controller;

import java.time.LocalDate;
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

import com.poly.BE_main.dto.CustomerDTO;
import com.poly.BE_main.dto.CustomerInfoDTO;
import com.poly.BE_main.dto.InformationCustomerDTO;
import com.poly.BE_main.service.CustomerService;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/customer")
public class CustomerController {
    @Autowired
    CustomerService customerService;

    @GetMapping("/show")
    public List<CustomerDTO> findAll() {
        return customerService.findAll();
    }

    @GetMapping("/showInfoCustomer/{customerId}")
    public InformationCustomerDTO findInformationCustomerByCustomerId(@PathVariable Integer customerId) {
        return customerService.findInformationCustomerByCustomerId(customerId);
    }

    @PostMapping("/add")
    public CustomerDTO add(@RequestBody CustomerDTO c) {
        System.out.println("Thêm thành công");
        System.out.println("Received: " + c);
        return customerService.create(c);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Integer id) {
        customerService.deleteById(id);
    }

    @PutMapping("/update/{id}")
    public CustomerDTO update(@PathVariable int id, @RequestBody CustomerDTO c) {
        System.out.println("Received: " + c);
        return customerService.update(id, c);
    }

    @PutMapping("/updateInfoCustomer/{id}")
    public ResponseEntity<?> updateCustomer(@PathVariable Integer id, @RequestBody InformationCustomerDTO dto) {
        LocalDate birthDate = dto.getBirthOfDate();
        customerService.updateCustomerInfo(
                id,
                dto.getFullName(),
                dto.getGender(),
                dto.getEmail(),
                dto.getNumberPhone(),
                birthDate);
        return ResponseEntity.ok("Updated successfully");
    }

    @GetMapping("/findByAccountId/{accountId}")
    public List<CustomerInfoDTO> findByAccountId(@PathVariable Integer accountId) {
        return customerService.findByAccountId(accountId);
    }
}