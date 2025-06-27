package com.poly.BE_main.service;

import java.util.List;
import com.poly.BE_main.model.Brand;

public interface BrandService {
    List<Brand> findAll();
    Brand findById(Long id);
    Brand save(Brand brand);
    void deleteById(Long id);
}
