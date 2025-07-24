package com.poly.BE_main.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.poly.BE_main.model.Image;

public interface ImageRepository extends JpaRepository<Image, Integer>{
    List<Image> findByProductDetailId(int productDetailId);
    long countByProductDetailId(int productDetailId);
}
