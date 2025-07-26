package com.poly.BE_main.controller;

import java.util.List;
import java.util.Map;

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
import com.poly.BE_main.model.BillDetail;
import com.poly.BE_main.service.BillDetailService;

@RestController
@RequestMapping("/billDetail")
@CrossOrigin("*")
public class BillDetailController {

    @Autowired
    BillDetailService billDetailService;

    @GetMapping("/show")
    public List<BillDetail> findAll(){
        return billDetailService.findAll();
    }

    @GetMapping("/show/{id}")
    public ResponseEntity<List<BillDetail>> getBillDetails(@PathVariable("id") Integer id) {
        List<BillDetail> details = billDetailService.findByBillId(id);
        return ResponseEntity.ok(details);
    }

    @PostMapping("/create")
    public BillDetail create(@RequestBody BillDetail i){
        return billDetailService.create(i);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Integer id){
        billDetailService.delete(id);
    }

    @PutMapping("/update/{id}")
    public BillDetail update(@PathVariable int id, @RequestBody BillDetail i){
        return billDetailService.update(id, i);
    }

    @PutMapping("/updateQuantity/{id}")
    public ResponseEntity<?> updateQuantityOnly(@PathVariable("id") Integer id,
                                                @RequestBody Map<String, Integer> body) {
        Integer quantity = body.get("quantity");
        if (quantity == null || quantity < 1) {
            return ResponseEntity.badRequest().body("Số lượng không hợp lệ.");
        }
        BillDetail updated = billDetailService.updateQuantity(id, quantity);
        return ResponseEntity.ok(updated);
    }

}