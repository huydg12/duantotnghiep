package com.poly.BE_main.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDetailDTO {
    private int cartDetailId;
    private int cartId;
    private int customerId;
    private int productId;
    private String productName;
    private int productDetailId;
    private String size;
    private String color;
    private BigDecimal price;
    private int quantity;
    private String images;
}
