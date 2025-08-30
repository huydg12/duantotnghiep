package com.poly.BE_main.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.poly.BE_main.model.Sole;
import com.poly.BE_main.service.SoleService;

@RestController
@RequestMapping("/sole")
public class SoleController {

    @Autowired
    SoleService soleService;

    @GetMapping("/show")
    public List<Sole> finall() {
        return soleService.findAll();
    }

    @PostMapping("/add")
    public Sole add(@RequestBody Sole s) {
        return soleService.create(s);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable int id) {
        soleService.delete(id);
    }

    @PutMapping("/update/{id}")
    public Sole update(@PathVariable int id, @RequestBody Sole s) {
        return soleService.update(id, s);
    }

    @PutMapping("/updateStatus/{id}")
    public Sole updateStatus(@PathVariable int id, @RequestBody Sole s) {
        return soleService.updateStatus(id, s);
    }
}
