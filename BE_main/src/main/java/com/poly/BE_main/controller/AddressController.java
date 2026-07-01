package com.poly.BE_main.controller;

import com.poly.BE_main.model.Address;
import com.poly.BE_main.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @PreAuthorize("@ownership.ownsCustomer(authentication, #a.customerId)")
    public Address add(@RequestBody Address a) {
        System.out.println("thêm thành công");
        return addressService.create(a);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("@ownership.ownsAddress(authentication, #id)")
    public void delete(@PathVariable Integer id) {
        addressService.deleteById(id);
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("@ownership.ownsAddress(authentication, #id) and @ownership.ownsCustomer(authentication, #a.customerId)")
    public Address update(@PathVariable int id, @RequestBody Address a) {

        return addressService.update(id, a);
    }

    @GetMapping("/showById/{customerId}")
    @PreAuthorize("@ownership.ownsCustomer(authentication, #customerId)")
    public List<Address> getByCustomerId(@PathVariable Integer customerId) {
        return addressService.findByCustomerId(customerId);
    }

    @PutMapping("/set-default/{id}")
    @PreAuthorize("@ownership.ownsAddress(authentication, #id)")
    public ResponseEntity<?> setDefaultAddress(@PathVariable("id") Integer id) {
        try {
            addressService.setDefaultAddress(id);
            return ResponseEntity.ok("Cập nhật địa chỉ mặc định thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }
}
