package com.poly.BE_main.service;

import java.util.List;
import com.poly.BE_main.model.ProductDetail;

public interface ProductDetailService {
    List<ProductDetail> findAll();
    ProductDetail findById(Long id);
    ProductDetail save(ProductDetail productdetail);
    void deleteById(Long id);
}
