package com.poly.BE_main.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.poly.BE_main.model.PaymentMethod;
import com.poly.BE_main.service.PaymentMethodService;

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
