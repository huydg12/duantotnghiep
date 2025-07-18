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

import com.poly.BE_main.model.Bill;
import com.poly.BE_main.service.BillService;
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/bill")
public class BillController {

    @Autowired
    BillService billService;

    @GetMapping("/show")
    public List<Bill> findALl(){
        return billService.findAll();
    }

    @PostMapping("/add")
    public ResponseEntity<Bill> create(@RequestBody Bill bill) {
        // Gán Bill vào từng BillDetail để duy trì quan hệ hai chiều
        if (bill.getBillDetails() != null) {
            bill.getBillDetails().forEach(detail -> detail.setBill(bill));
        }

        Bill savedBill = billService.create(bill);
        return ResponseEntity.ok(savedBill);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Integer id){
        billService.delete(id);
    }

    @PutMapping("/update/{id}")
    public Bill update(@PathVariable int id, @RequestBody Bill i){
        return billService.update(id, i);
    }

    
}