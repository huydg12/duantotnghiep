package com.poly.BE_main.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.poly.BE_main.dto.RevenueByBrandDTO;
import com.poly.BE_main.dto.StockStatisticDTO;
import com.poly.BE_main.dto.TopSellingProductDTO;

public interface StatisticService {
    Map<String, Object> getRevenueOverview();

    List<TopSellingProductDTO> getTopSellingProducts();

    List<TopSellingProductDTO> getTopSellingProductsBetween(LocalDate from, LocalDate to);

    List<RevenueByBrandDTO> getRevenueByBrand();

    List<StockStatisticDTO> getStockStatistics();

    List<RevenueByBrandDTO> getRevenueByBrandFiltered(LocalDate from, LocalDate to);

    List<StockStatisticDTO> getStockStatisticsFiltered(LocalDate from, LocalDate to);

}
