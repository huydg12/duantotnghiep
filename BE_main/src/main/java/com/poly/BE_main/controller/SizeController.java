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

import com.poly.BE_main.model.Size;
import com.poly.BE_main.service.SizeService;

@RestController
@RequestMapping("/size")
public class SizeController {
    @Autowired
    SizeService sizeService;

    @GetMapping("/show")
    public List<Size> findAll() {
        return sizeService.findAll();
    }

    @PostMapping("/add")
    public Size add(@RequestBody Size s) {
        return sizeService.create(s);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable int id) {
        sizeService.delete(id);
    }

    @PutMapping("/update/{id}")
    public Size update(@PathVariable int id, @RequestBody Size s) {
        return sizeService.update(id, s);
    }
}