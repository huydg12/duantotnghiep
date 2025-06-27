package com.poly.BE_main.service;

import java.util.List;
import com.poly.BE_main.model.Product;

public interface ProductService {
    List<Product> findAll();
    Product findById(Long id);
    Product save(Product product);
    void deleteById(Long id);
}
