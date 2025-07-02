package com.poly.BE_main.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// import com.poly.BE_main.dto.PromotionDetailDTO;
import com.poly.BE_main.service.PromotionDetailService;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/promotionDetail")
public class PromotionDetailController {
    @Autowired
    PromotionDetailService promotionDetailService;

    @GetMapping("/show/{promotionId}")
    public ResponseEntity<List<PromotionDetailDTO>> showPromotionDetail(@PathVariable Integer promotionId){
        List<PromotionDetailDTO> list = promotionDetailService.findAllPromotionDetailDTOByID(promotionId);
        return ResponseEntity.ok(list);
    }
}