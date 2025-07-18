package com.poly.BE_main.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.poly.BE_main.model.PromotionDetail;

public interface PromotionDetailRepository extends JpaRepository<PromotionDetail, Integer> {
    List<PromotionDetail> findByPromotionId(Integer promotionId);
}
