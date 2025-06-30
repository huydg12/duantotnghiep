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

    public void deleteById(Integer id) {
        productRepository.deleteById(id);
    }

    public Product update(int id, Product pUpdate) {
        return productRepository.findById(id).map(p -> {
            p.setProductName(pUpdate.getProductName());
            p.setBrandId(pUpdate.getBrandId());
            p.setStyleId(pUpdate.getStyleId());
            p.setSoleId(pUpdate.getSoleId());
            p.setDescription(pUpdate.getDescription());
            p.setCreatedBy(pUpdate.getCreatedBy());
            p.setCreatedDate(pUpdate.getCreatedDate());
            p.setStatus(pUpdate.getStatus());
            return productRepository.save(p);
        }).orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm có id: " + id));
    }

}
