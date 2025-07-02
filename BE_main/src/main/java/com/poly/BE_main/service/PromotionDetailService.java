package com.poly.BE_main.service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// import com.poly.BE_main.dto.ProductDetailDTO;
import com.poly.BE_main.repository.PromotionDetailRepository;

@Service
public class PromotionDetailService {
    @Autowired
    PromotionDetailRepository promotionDetailRepository;

    public List<PromotionDetailDTO> findAllPromotionDetailDTOByID(Integer promotionId){
        List<Object[]> results = promotionDetailRepository.findAllProductDetailDTOByID(promotionId);
        
        return results.stream().map(obj -> {
            Integer promotion 
        })
    }

}
