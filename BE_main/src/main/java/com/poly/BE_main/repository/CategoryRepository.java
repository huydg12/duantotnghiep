package com.poly.BE_main.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.poly.BE_main.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

}