package com.poly.BE_main.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.poly.BE_main.dto.ProductDTO;
import com.poly.BE_main.model.Product;
import com.poly.BE_main.service.ProductService;
@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/product")
public class ProductController {
@Autowired
ProductService productService;

@GetMapping("/show")
public List<Product>findAll(){
    return productService.findAll();
}
@GetMapping("/showSPdto")
public ResponseEntity<List<ProductDTO>> getAllProductDTOs() {
    List<ProductDTO> products = productService.getProducts();
    return ResponseEntity.ok(products); // Trả về 200 OK với danh sách ProductDTO
}

@PostMapping("/add")
public Product add(@RequestBody Product p){
    System.out.println("thêm ok");
    return productService.create(p);
    
}

@DeleteMapping("/delete/{id}")
public void delete(@PathVariable Integer id){
    productService.deleteById(id);
}

@PutMapping("/update/{id}")
public Product update(@PathVariable int id, @RequestBody Product p){
    return productService.update(id, p);
}
}