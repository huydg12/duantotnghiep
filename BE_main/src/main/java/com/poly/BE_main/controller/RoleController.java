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

import com.poly.BE_main.model.Role;
import com.poly.BE_main.service.RoleService;

@RestController
@RequestMapping("/role")
public class RoleController {
    @Autowired
    RoleService roleService;

    @GetMapping("/show")
    public List<Role> findAll() {
        return roleService.findAll();
    }

    @PostMapping("/add")
    public Role add(@RequestBody Role r) {
        return roleService.create(r);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable int id) {
        roleService.delete(id);
    }

    @PutMapping("/update/{id}")
    public Role update(@PathVariable int id, @RequestBody Role r) {
        return roleService.update(id, r);
    }
}
