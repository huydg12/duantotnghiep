package com.poly.BE_main.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poly.BE_main.model.Brand;
import com.poly.BE_main.repository.BrandRepository;

@Service
public class BrandService {
    @Autowired
    BrandRepository brandRRepository;

    public List<Brand> findAll() {
        return brandRRepository.findAll();
    }

    public Brand create(Brand b) {
        brandRRepository.save(b);
        return b;
    }

    public void deleteById(Integer id) {
        brandRRepository.deleteById(id);
    }

    public Brand update(int id, Brand bUpdate) {
        return brandRRepository.findById(id).map(b -> {
            b.setName(bUpdate.getName());
            b.setDescription(bUpdate.getDescription());
            return brandRRepository.save(b);
        }).orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu có id: " + id));
    }
}
