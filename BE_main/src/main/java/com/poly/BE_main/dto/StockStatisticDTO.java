package com.poly.BE_main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StockStatisticDTO {
    private String productName;
    private String color;
    private String size;
    private Long totalSold;         // đã bán
}

