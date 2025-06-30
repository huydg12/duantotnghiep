package com.poly.BE_main.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.poly.BE_main.model.PromotionDetail;
@Service
public interface PromotionDetailService {
    List<PromotionDetail> findAll();
    PromotionDetail findById(Long id);
    PromotionDetail save(PromotionDetail promotiondetail);
    void deleteById(Long id);
}
