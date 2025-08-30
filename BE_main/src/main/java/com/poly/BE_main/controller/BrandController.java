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

import com.poly.BE_main.model.Brand;
import com.poly.BE_main.service.BrandService;

@RestController
@RequestMapping("/brand")
public class BrandController {
    @Autowired
    BrandService brandService;

    @GetMapping("/show")
    public List<Brand> findAll() {
        return brandService.findAll();
    }

    @PostMapping("/add")
    public Brand add(@RequestBody Brand b) {
        return brandService.create(b);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Integer id) {
        brandService.deleteById(id);
    }

    @PutMapping("/update/{id}")
    public Brand update(@PathVariable int id, @RequestBody Brand b) {
        return brandService.update(id, b);
    }

    @PutMapping("/updateStatus/{id}")
    public Brand updateStatus(@PathVariable int id, @RequestBody Brand b) {
        return brandService.updateStatus(id, b);
    }
}