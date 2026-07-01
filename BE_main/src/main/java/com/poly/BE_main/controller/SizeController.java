package com.poly.BE_main.controller;

import com.poly.BE_main.model.Size;
import com.poly.BE_main.service.SizeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PutMapping("/updateStatus/{id}")
    public Size updateStatus(@PathVariable int id, @RequestBody Size s) {
        return sizeService.updateStatus(id, s);
    }
}