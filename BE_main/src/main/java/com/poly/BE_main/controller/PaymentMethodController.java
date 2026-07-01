package com.poly.BE_main.controller;

import com.poly.BE_main.model.PaymentMethod;
import com.poly.BE_main.service.PaymentMethodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/PaymentMethod")
public class PaymentMethodController {
    @Autowired
    PaymentMethodService paymentMethodService;

    @GetMapping("/show")
    public List<PaymentMethod> findAll() {
        return paymentMethodService.findAll();
    }

    @PostMapping("/add")
    public PaymentMethod add(@RequestBody PaymentMethod p) {
        return paymentMethodService.create(p);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable int id) {
        paymentMethodService.delete(id);
    }

    @PutMapping("/update/{id}")
    public PaymentMethod update(@PathVariable int id, @RequestBody PaymentMethod p) {
        return paymentMethodService.update(id, p);
    }
}
