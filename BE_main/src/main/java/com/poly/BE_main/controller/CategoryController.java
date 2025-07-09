package com.poly.BE_main.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.poly.BE_main.model.Category;
import com.poly.BE_main.service.CategoryService;

import org.springframework.web.bind.annotation.PutMapping;


@RestController
@RequestMapping("/style")
public class CategoryController {
@Autowired
CategoryService categoryService;

@GetMapping("/show")
public List<Category>FindAll(){
    return categoryService.findAll();
}

@PostMapping("/add")
public Category add(@RequestBody Category category){
    return categoryService.create(category);
}

@DeleteMapping("/delete/{id}")
public void delete(@PathVariable Integer id){
    categoryService.delete(id);
}

@PutMapping("/update/{id}")
public Category update(@PathVariable Integer id, @RequestBody Category category) {    
    return categoryService.update(id, category);
}
}