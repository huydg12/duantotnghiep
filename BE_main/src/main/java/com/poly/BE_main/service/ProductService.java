package com.poly.BE_main.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poly.BE_main.model.Product;
import com.poly.BE_main.repository.ProductRepository;

@Service
public class ProductService {
    @Autowired
    ProductRepository productRepository;

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    public Product create(Product p) {
        productRepository.save(p);
        return p;
    }
}
