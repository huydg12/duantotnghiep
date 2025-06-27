package com.poly.BE_main.service;

import java.util.List;
import com.poly.BE_main.model.PromotionDetail;

public interface PromotionDetailService {
    List<PromotionDetail> findAll();
    PromotionDetail findById(Long id);
    PromotionDetail save(PromotionDetail promotiondetail);
    void deleteById(Long id);
}
