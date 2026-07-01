package com.poly.BE_main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailDTO {
    private Integer productId;
    private Integer productDetailId;
    private String productName;
    private String brandName;
    private String color;
    private String collar;
    private String description;
    private String size;
    private BigDecimal price;
    private Integer quantity;
    private List<String> images;
    private boolean isActive;
}
