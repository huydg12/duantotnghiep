package com.poly.BE_main.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.poly.BE_main.dto.ProductDTO;
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

    public Product updadte(Product p) {
        return productRepository.save(p);
    }

    public Product update(int id, Product pUpdate) {
        return productRepository.findById(id).map(p -> {
            p.setProductName(pUpdate.getProductName());
            p.setBrandId(pUpdate.getBrandId());
            p.setCategoryId(pUpdate.getCategoryId());
            p.setSoleId(pUpdate.getSoleId());
            p.setDescription(pUpdate.getDescription());
            p.setCreatedBy(pUpdate.getCreatedBy());
            p.setCreatedDate(pUpdate.getCreatedDate());
            return productRepository.save(p);
        }).orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm có id: " + id));
    }

    public Product updateStatus(int id, Product pUpdate) {
        return productRepository.findById(id).map(p -> {
            if (p.getStatus().equals(1)) {
                p.setStatus(0);
            }else{
                p.setStatus(1);
            }
            return productRepository.save(p);
        }).orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm có id: " + id));
    }

    public List<ProductDTO> getProducts() {
        List<Object[]> results = productRepository.findAllProductsWithImages();

        List<ProductDTO> products = new ArrayList<>();

        for (Object[] row : results) {
            Integer productId = ((Number) row[0]).intValue();
            String productName = (String) row[1];
            String brandName = (String) row[2];
            BigDecimal price = (BigDecimal) row[3];
            String image1 = (String) row[4]; // Ảnh chính
            String image2 = (String) row[5]; // Ảnh hover

            products.add(new ProductDTO(productId, productName, brandName, price, image1, image2));
        }

        return products;
    }

    public List<ProductDTO> searchByKeyword(String keyword) {
        List<Object[]> results = productRepository.searchByKeyword("%" + keyword + "%");
        List<ProductDTO> products = new ArrayList<>();

        for (Object[] row : results) {
            Integer productId = ((Number) row[0]).intValue();
            String productName = (String) row[1];
            String brandName = (String) row[2];
            BigDecimal price = (BigDecimal) row[3];
            String image1 = (String) row[4];
            String image2 = (String) row[5];

            products.add(new ProductDTO(productId, productName, brandName, price, image1, image2));
        }

        return products;
    }

    public List<ProductDTO> getTop4ListProducts() {
        List<Object[]> results = productRepository.findTop4ListProucts();
        return results.stream().map(row -> new ProductDTO(
                (Integer) row[0], // productId
                (String) row[1], // productName
                (String) row[2], // brandName
                (BigDecimal) row[3], // price
                (String) row[4], // image1
                (String) row[5] // image2
        )).collect(Collectors.toList());
    }

}
