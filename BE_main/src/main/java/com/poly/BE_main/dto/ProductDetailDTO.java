package com.poly.BE_main.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String descriptionProduct;
    private String size;
    private BigDecimal price;
    private List<String> images;
}
