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

import com.poly.BE_main.dto.PromotionDetailDTO;
import com.poly.BE_main.model.PromotionDetail;
import com.poly.BE_main.service.PromotionDetailService;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/promotionDetail")
public class PromotionDetailController {

    @Autowired
    PromotionDetailService service;

    @GetMapping("/show")
    public List<PromotionDetail> getAll() {
        return service.findAll();
    }

    @GetMapping("/by-promotion/{promotionId}")
    public List<PromotionDetailDTO> getByPromotion(@PathVariable Integer promotionId) {
        return service.getDetailsByPromotionId(promotionId);
    }

    @PostMapping("/add")
    public PromotionDetail add(@RequestBody PromotionDetail detail) {
        return service.create(detail);
    }

    @PutMapping("/update/{id}")
    public PromotionDetail update(@PathVariable Integer id, @RequestBody PromotionDetail detail) {
        return service.update(id, detail);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Integer id) {
        service.delete(id);
    }
}
