package com.poly.BE_main.service;

import java.util.List;
import com.poly.BE_main.model.Promotion;

public interface PromotionService {
    List<Promotion> findAll();
    Promotion findById(Long id);
    Promotion save(Promotion promotion);
    void deleteById(Long id);
}
