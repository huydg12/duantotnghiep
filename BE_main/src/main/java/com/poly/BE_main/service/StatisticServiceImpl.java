package com.poly.BE_main.service;

import com.poly.BE_main.dto.RevenueByBrandDTO;
import com.poly.BE_main.dto.StockStatisticDTO;
import com.poly.BE_main.dto.TopSellingProductDTO;
import com.poly.BE_main.repository.BillDetailRepository;
import com.poly.BE_main.repository.BillRepository;
import com.poly.BE_main.repository.ProductDetailRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatisticServiceImpl implements StatisticService {

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private ProductDetailRepository productDetailRepo;

    @Autowired
    private BillDetailRepository billDetailRepo;

    /**
     * Tổng quan doanh thu:
     * - Tổng doanh thu
     * - Số đơn đã thanh toán
     * - Tổng sản phẩm đã bán
     * - Số đơn có khuyến mãi
     * - Tổng tiền đã giảm giá
     */
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

    /**
     * Lấy top sản phẩm bán chạy nhất (top 10)
     */
    @Override
    public List<TopSellingProductDTO> getTopSellingProducts() {
        return billRepository.getTopSellingProducts();
    }

    /**
     * Lấy top sản phẩm bán chạy trong khoảng thời gian
     */
    @Override
    public List<TopSellingProductDTO> getTopSellingProductsBetween(LocalDate from, LocalDate to) {
        return billRepository.getTopSellingProductsBetween(from, to);
    }

    /**
     * Thống kê doanh thu theo thương hiệu
     */
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

}
