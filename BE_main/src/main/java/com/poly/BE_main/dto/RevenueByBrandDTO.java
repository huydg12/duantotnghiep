package com.poly.BE_main.dto;

import java.math.BigDecimal;

public class RevenueByBrandDTO {
    private String brandName;
    private BigDecimal totalRevenue;

    public RevenueByBrandDTO(String brandName, BigDecimal totalRevenue) {
        this.brandName = brandName;
        this.totalRevenue = totalRevenue;
    }

    public String getBrandName() {
        return brandName;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }
}
