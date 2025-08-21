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

    // --- Tổng quan ---
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> getOverview() {
        Map<String, Object> data = statisticService.getRevenueOverview();
        return ResponseEntity.ok(data);
    }

    // --- Top sản phẩm bán chạy ---
    @GetMapping("/top-selling-products")
    public ResponseEntity<List<TopSellingProductDTO>> getTopSellingProducts() {
        return ResponseEntity.ok(statisticService.getTopSellingProducts());
    }

    // Filter top-selling có ngày và keyword
    @GetMapping("/top-selling-products/filter")
    public ResponseEntity<List<TopSellingProductDTO>> getTopSellingProductsFiltered(
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(value = "keyword", required = false) String keyword) {

        return ResponseEntity.ok(statisticService.getTopSellingProductsFiltered(from, to, keyword));
    }

    // --- Doanh thu theo thương hiệu ---
    @GetMapping("/revenue-by-brand")
    public ResponseEntity<List<RevenueByBrandDTO>> getRevenueByBrand() {
        return ResponseEntity.ok(statisticService.getRevenueByBrand());
    }

    // Filter revenue-by-brand có ngày và keyword
    @GetMapping("/revenue-by-brand/filter")
    public ResponseEntity<List<RevenueByBrandDTO>> getRevenueByBrandFiltered(
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(value = "keyword", required = false) String keyword) {

        return ResponseEntity.ok(statisticService.getRevenueByBrandFiltered(from, to, keyword));
    }

    // --- Tồn kho đã bán ---
    @GetMapping("/stock")
    public List<StockStatisticDTO> getStockStatistics() {
        return statisticService.getStockStatistics();
    }

    // Filter stock có ngày và keyword
    @GetMapping("/stock/filter")
    public ResponseEntity<List<StockStatisticDTO>> getStockStatisticsFiltered(
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(value = "keyword", required = false) String keyword) {

        return ResponseEntity.ok(statisticService.getStockStatisticsFiltered(from, to, keyword));
    }
}
