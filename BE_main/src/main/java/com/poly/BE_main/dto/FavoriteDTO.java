package com.poly.BE_main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FavoriteDTO {
    private Integer favoriteId;
    private Integer customerId;
    private Integer productId;
    private String productName;
    private String brandName;
    private BigDecimal price;
    private String image1;
    private String image2;
}
