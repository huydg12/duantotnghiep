package com.poly.BE_main.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import com.poly.BE_main.model.Promotion;
import com.poly.BE_main.repository.PromotionRepository;

@Service
public class PromotionService {
    @Autowired
    PromotionRepository promotionRepository;

    public List<Promotion> findAll(){
        return promotionRepository.findAll();
    }

    public Promotion create(Promotion p){
        promotionRepository.save(p);
        return p;
    }

    public void deleteById(Integer id){
        promotionRepository.deleteById(id);
    }

    public Promotion update(Promotion p){
        return promotionRepository.save(p);
    }

        public Promotion update(int id, Promotion pUpdate) {
        return promotionRepository.findById(id).map(p -> {
            p.setPromotionCode(pUpdate.getPromotionCode());
            p.setName(pUpdate.getName());
            p.setType(pUpdate.getType());
            p.setValue(pUpdate.getValue());
            p.setStartDate(pUpdate.getStartDate());
            p.setEndDate(pUpdate.getEndDate());
            p.setStatus(pUpdate.getStatus());
            p.setNote(pUpdate.getNote());
            p.setCreatedDate(pUpdate.getCreatedDate());
            p.setModifiedDate(pUpdate.getModifiedDate());
            return promotionRepository.save(p);
        }).orElseThrow(() -> new RuntimeException("Không tìm thấy khuyến mãi với id: " + id));
    }

}
