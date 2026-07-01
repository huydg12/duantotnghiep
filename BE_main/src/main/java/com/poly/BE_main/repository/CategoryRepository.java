package com.poly.BE_main.repository;

import com.poly.BE_main.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

}