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

import com.poly.BE_main.model.Promotion;
import com.poly.BE_main.service.PromotionService;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/promotion")
public class PromotionController {
    @Autowired
    PromotionService promotionService;

    @GetMapping("/show")
    public List<Promotion> findAll() {
        return promotionService.findAll();
    }

    @PostMapping("/add")
    public Promotion add(@RequestBody Promotion p) {
        System.out.println("thêm thành công");
        return promotionService.create(p);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Integer id) {
        promotionService.deleteById(id);
    }

    @PutMapping("/update/{id}")
    public Promotion update(@PathVariable int id, @RequestBody Promotion p) {
        return promotionService.update(id, p);
    }

    @PutMapping("/updateStatus/{id}")
    public Promotion updateStatus(@PathVariable int id, @RequestBody Promotion p) {
        return promotionService.updateStatus(id, p);
    }

}