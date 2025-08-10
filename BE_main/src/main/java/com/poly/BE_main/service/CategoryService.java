package com.poly.BE_main.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poly.BE_main.model.Category;
import com.poly.BE_main.repository.CategoryRepository;

@Service
public class CategoryService {

    @Autowired
    CategoryRepository categoryRepository;

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public Category create(Category category) {
        return categoryRepository.save(category);
    }

    public void delete(Integer id) {
        categoryRepository.deleteById(id);
    }

    public Category update(int id, Category category) {
        return categoryRepository.findById(id).map(s -> {
            s.setName(category.getName());
            s.setDescription(category.getDescription());
            return categoryRepository.save(s);
        }).orElseThrow(() -> new RuntimeException("Không tìm thấy category có id: " + id));
    }
}
