package com.poly.BE_main.controller;

import com.poly.BE_main.dto.CustomerDTO;
import com.poly.BE_main.dto.CustomerInfoDTO;
import com.poly.BE_main.dto.InformationCustomerDTO;
import com.poly.BE_main.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/customer")
public class CustomerController {
    @Autowired
    CustomerService customerService;

    @GetMapping("/show")
    @PreAuthorize("@ownership.isStaff(authentication)")
    public List<CustomerDTO> findAll() {
        return customerService.findAll();
    }

    @GetMapping("/page")
    @PreAuthorize("@ownership.isStaff(authentication)")
    public ResponseEntity<?> page(@RequestParam(defaultValue = "") String keyword,
                                  @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(customerService.findPage(keyword, page, size));
    }

    @GetMapping("/showInfoCustomer/{customerId}")
    @PreAuthorize("@ownership.ownsCustomer(authentication, #customerId)")
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

    @PutMapping("/updateStatus/{id}")
    public CustomerDTO updateStatus(@PathVariable int id, @RequestBody CustomerDTO c) {
        System.out.println("Received: " + c);
        return customerService.updateStatus(id, c);
    }

    @PutMapping("/updateInfoCustomer/{id}")
    @PreAuthorize("@ownership.ownsCustomer(authentication, #id)")
    public ResponseEntity<?> updateCustomer(@PathVariable Integer id, @RequestBody InformationCustomerDTO dto) {
        LocalDate birthDate = dto.getBirthOfDate();
        customerService.updateCustomerInfo(
                id,
                dto.getFullName(),
                dto.isGender(),
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
