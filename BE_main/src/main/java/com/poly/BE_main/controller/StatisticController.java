package com.poly.BE_main.controller;

import com.poly.BE_main.dto.StatisticBillDTO;
import com.poly.BE_main.dto.StatisticTop5SPDTO;
import com.poly.BE_main.service.StatisticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/statistics")
@CrossOrigin("*")
public class StatisticController {

    @Autowired
    private StatisticService statisticService;

    @GetMapping("/daily")
    public ResponseEntity<List<StatisticBillDTO>> getRevenueDaily() {
        return ResponseEntity.ok(statisticService.getDailyRevenue());
    }

    @GetMapping("/top5-best-selling")
    public ResponseEntity<List<StatisticTop5SPDTO>> getTop5BestSellingProductsPaid() {
        List<StatisticTop5SPDTO> products = statisticService.getTop5BestSellingProductsPaid();
        return ResponseEntity.ok(products);
    }
}
