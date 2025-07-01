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

import com.poly.BE_main.model.Color;
import com.poly.BE_main.service.ColorService;

@RestController
@RequestMapping("/color")
public class ColorController {

    @Autowired
    ColorService colorService;

    @GetMapping("/show")
    public List<Color> FinAll() {
        return colorService.FinAll();
    }

    @PostMapping("/add")
    public Color add(@RequestBody Color c) {
        return colorService.create(c);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Integer id) {
        colorService.deleteById(id);
    }

    @PutMapping("/update/{id}")
    public Color update(@PathVariable int id, @RequestBody Color c) {
        return colorService.update(id, c);
    }
}