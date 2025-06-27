package com.poly.BE_main.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.poly.BE_main.model.Product;
import com.poly.BE_main.service.ProductService;

@RestController
@RequestMapping("/product")
public class ProductController {
@Autowired
ProductService productService;

@GetMapping("/show")
public List<Product>findAll(){
    return productService.findAll();
}
}