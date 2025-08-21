package com.poly.BE_main.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poly.BE_main.dto.RevenueByBrandDTO;
import com.poly.BE_main.dto.StockStatisticDTO;
import com.poly.BE_main.dto.TopSellingProductDTO;
import com.poly.BE_main.repository.BillDetailRepository;
import com.poly.BE_main.repository.BillRepository;
import com.poly.BE_main.repository.ProductDetailRepository;

// Interface
public interface StatisticService {
    Map<String, Object> getRevenueOverview();

    List<TopSellingProductDTO> getTopSellingProducts();

    List<TopSellingProductDTO> getTopSellingProductsBetween(LocalDate from, LocalDate to);

    List<RevenueByBrandDTO> getRevenueByBrand();

    List<StockStatisticDTO> getStockStatistics();

    List<RevenueByBrandDTO> getRevenueByBrandFiltered(LocalDate from, LocalDate to);

    List<StockStatisticDTO> getStockStatisticsFiltered(LocalDate from, LocalDate to);

    List<TopSellingProductDTO> getTopSellingProductsFiltered(LocalDate from, LocalDate to, String keyword);

    List<RevenueByBrandDTO> getRevenueByBrandFiltered(LocalDate from, LocalDate to, String keyword);

    List<StockStatisticDTO> getStockStatisticsFiltered(LocalDate from, LocalDate to, String keyword);

}

// Implementation
@Service
class StatisticServiceImpl implements StatisticService {

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private ProductDetailRepository productDetailRepo;

    @Autowired
    private BillDetailRepository billDetailRepo;

    @Override
    public Map<String, Object> getRevenueOverview() {
        Map<String, Object> result = new HashMap<>();
        result.put("totalRevenue", billRepository.getTotalRevenue());
        result.put("paidBillCount", billRepository.getPaidBillCount());
        result.put("totalProductSold", billRepository.getTotalProductSold());
        result.put("promoBillCount", billRepository.getPromoBillCount());
        result.put("totalDiscountAmount", billRepository.getTotalDiscountAmount());
        return result;
    }

    @Override
    public List<TopSellingProductDTO> getTopSellingProducts() {
        return billRepository.getTopSellingProducts();
    }

    @Override
    public List<TopSellingProductDTO> getTopSellingProductsBetween(LocalDate from, LocalDate to) {
        return billRepository.getTopSellingProductsBetween(from, to);
    }

    @Override
    public List<RevenueByBrandDTO> getRevenueByBrand() {
        return billRepository.getRevenueByBrand();
    }

    @Override
    public List<StockStatisticDTO> getStockStatistics() {
        return billRepository.getStockStatistics();
    }

    @Override
    public List<RevenueByBrandDTO> getRevenueByBrandFiltered(LocalDate from, LocalDate to) {
        return billRepository.getRevenueByBrandFiltered(from, to);
    }

    @Override
    public List<StockStatisticDTO> getStockStatisticsFiltered(LocalDate from, LocalDate to) {
        return billRepository.getStockStatisticsFiltered(from, to);
    }

    // Top-selling
    public List<TopSellingProductDTO> getTopSellingProductsFiltered(LocalDate from, LocalDate to, String keyword) {
        return null;
    }

    // Revenue by brand
    public List<RevenueByBrandDTO> getRevenueByBrandFiltered(LocalDate from, LocalDate to, String keyword) {
        return null;
    }

    // Stock statistics
    public List<StockStatisticDTO> getStockStatisticsFiltered(LocalDate from, LocalDate to, String keyword) {
        return null;
    }

}
