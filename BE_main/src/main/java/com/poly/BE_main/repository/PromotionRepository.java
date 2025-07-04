package com.poly.BE_main.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.poly.BE_main.model.Promotion;

public interface PromotionRepository extends JpaRepository<Promotion, Integer> {

}