package com.poly.BE_main.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poly.BE_main.model.Promotion;
import com.poly.BE_main.repository.PromotionRepository;

@Service
public class PromotionService {
    @Autowired
    PromotionRepository promotionRepository;

    public List<Promotion> findAll() {
        return promotionRepository.findAll();
    }

    public Promotion create(Promotion p) {
        validate(p);
        if (promotionRepository.existsByPromotionCodeIgnoreCase(p.getPromotionCode()))
            throw new IllegalArgumentException("Promotion code already exists");
        promotionRepository.save(p);
        return p;
    }

    public void deleteById(Integer id) {
        promotionRepository.deleteById(id);
    }

    public Promotion update(Promotion p) {
        return promotionRepository.save(p);
    }

    public Promotion update(int id, Promotion pUpdate) {
        validate(pUpdate);
        if (promotionRepository.existsByPromotionCodeIgnoreCaseAndIdNot(pUpdate.getPromotionCode(), id))
            throw new IllegalArgumentException("Promotion code already exists");
        return promotionRepository.findById(id).map(p -> {
            p.setPromotionCode(pUpdate.getPromotionCode());
            p.setName(pUpdate.getName());
            p.setType(pUpdate.getType());
            p.setValue(pUpdate.getValue());
            p.setMinOrderAmount(pUpdate.getMinOrderAmount());
            p.setMaxDiscountAmount(pUpdate.getMaxDiscountAmount());
            p.setUsageLimit(pUpdate.getUsageLimit());
            p.setPerCustomerLimit(pUpdate.getPerCustomerLimit());
            p.setStartDate(pUpdate.getStartDate());
            p.setEndDate(pUpdate.getEndDate());
            p.setActive(pUpdate.isActive());
            p.setNote(pUpdate.getNote());
            p.setCreatedDate(pUpdate.getCreatedDate());
            p.setModifiedDate(pUpdate.getModifiedDate());
            return promotionRepository.save(p);
        }).orElseThrow(() -> new RuntimeException("Không tìm thấy khuyến mãi với id: " + id));
    }

    private void validate(Promotion p) {
        if (p.getPromotionCode() == null || p.getPromotionCode().isBlank())
            throw new IllegalArgumentException("Promotion code is required");
        if (p.getType() == null || (p.getType() != 1 && p.getType() != 2))
            throw new IllegalArgumentException("Promotion type must be percentage or fixed amount");
        if (p.getValue() == null || p.getValue().signum() <= 0 || (p.getType() == 1 && p.getValue().compareTo(java.math.BigDecimal.valueOf(100)) > 0))
            throw new IllegalArgumentException("Promotion value is invalid");
        if (p.getStartDate() == null || p.getEndDate() == null || !p.getEndDate().isAfter(p.getStartDate()))
            throw new IllegalArgumentException("Promotion date range is invalid");
        if (p.getMinOrderAmount() != null && p.getMinOrderAmount().signum() < 0) throw new IllegalArgumentException("Minimum order cannot be negative");
        if (p.getMaxDiscountAmount() != null && p.getMaxDiscountAmount().signum() <= 0) throw new IllegalArgumentException("Maximum discount must be positive");
        if (p.getUsageLimit() != null && p.getUsageLimit() <= 0) throw new IllegalArgumentException("Usage limit must be positive");
        if (p.getPerCustomerLimit() != null && p.getPerCustomerLimit() <= 0) throw new IllegalArgumentException("Customer usage limit must be positive");
    }

    public Promotion updateStatus(int id, Promotion pUpdate) {
        return promotionRepository.findById(id).map(p -> {
            if (p.isActive() == true) {
                p.setActive(false);
            } else {
                p.setActive(true);
            }

            return promotionRepository.save(p);
        }).orElseThrow(() -> new RuntimeException("Không tìm thấy khuyến mãi với id: " + id));
    }

}
