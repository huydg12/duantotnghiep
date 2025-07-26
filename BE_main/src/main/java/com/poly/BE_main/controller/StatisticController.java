package com.poly.BE_main.controller;

import com.poly.BE_main.dto.RevenueByBrandDTO;
import com.poly.BE_main.dto.StockStatisticDTO;
import com.poly.BE_main.dto.TopSellingProductDTO;
import com.poly.BE_main.service.StatisticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@CrossOrigin("*")
public class StatisticController {

    @Autowired
    private StatisticService statisticService;

    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getOverview() {
        Map<String, Object> data = statisticService.getRevenueOverview();
        return ResponseEntity.ok(data);
    }

    @GetMapping("/top-selling-products")
    public ResponseEntity<List<TopSellingProductDTO>> getTopSellingProducts() {
        return ResponseEntity.ok(statisticService.getTopSellingProducts());
    }

    @GetMapping("/top-selling-products/filter")
    public ResponseEntity<List<TopSellingProductDTO>> getTopSellingProductsBetween(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(statisticService.getTopSellingProductsBetween(from, to));
    }

    @GetMapping("/revenue-by-brand")
    public ResponseEntity<List<RevenueByBrandDTO>> getRevenueByBrand() {
        return ResponseEntity.ok(statisticService.getRevenueByBrand());
    }

    // Doanh thu theo thương hiệu có lọc thời gian
    @GetMapping("/revenue-by-brand/filter")
    public ResponseEntity<List<RevenueByBrandDTO>> getRevenueByBrandFiltered(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(statisticService.getRevenueByBrandFiltered(from, to));
    }

    @GetMapping("/stock")
    public List<StockStatisticDTO> getStockStatistics() {
        return statisticService.getStockStatistics();
    }

    // Tồn kho đã bán có lọc thời gian
    @GetMapping("/stock/filter")
    public ResponseEntity<List<StockStatisticDTO>> getStockStatisticsFiltered(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(statisticService.getStockStatisticsFiltered(from, to));
    }

}
