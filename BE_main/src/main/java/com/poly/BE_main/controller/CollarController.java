package com.poly.BE_main.controller;

import com.poly.BE_main.model.Collar;
import com.poly.BE_main.service.CollarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/collar")
public class CollarController {
    @Autowired
    CollarService collarService;

    @GetMapping("/show")
    public List<Collar> findAll() {
        return collarService.findAll();
    }

    @PostMapping("/add")
    public Collar create(@RequestBody Collar c) {
        return collarService.create(c);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable int id) {
        collarService.delete(id);
    }

    @PutMapping("/update/{id}")
    public Collar update(@PathVariable int id, @RequestBody Collar c) {
        return collarService.update(id, c);
    }

    @PutMapping("/updateStatus/{id}")
    public Collar updateStatus(@PathVariable int id, @RequestBody Collar c) {
        return collarService.updateStatus(id, c);
    }
}
