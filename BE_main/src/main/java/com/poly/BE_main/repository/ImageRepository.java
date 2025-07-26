package com.poly.BE_main.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.poly.BE_main.model.Image;

import jakarta.transaction.Transactional;

public interface ImageRepository extends JpaRepository<Image, Integer>{
    List<Image> findByProductDetailId(int productDetailId);
    long countByProductDetailId(int productDetailId);
    

@Modifying
@Transactional
@Query(value = "UPDATE IMAGE SET IS_MAIN = 0 WHERE PRODUCT_DETAIL_ID = :productDetailId", nativeQuery = true)
void resetMainImageNative(@Param("productDetailId") Integer productDetailId);

@Modifying
@Transactional
@Query(value = "UPDATE IMAGE SET IS_MAIN = 1 WHERE ID = :imageId", nativeQuery = true)
void setMainImageById(@Param("imageId") Integer imageId);
}
