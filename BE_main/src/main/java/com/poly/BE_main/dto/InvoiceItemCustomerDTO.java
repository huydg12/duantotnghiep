package com.poly.BE_main.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class InvoiceItemCustomerDTO {
    private int productDetailId;
    private String name;
    private String color;
    private String size;
    private int quantity;
    private BigDecimal price;
    private String image;
    private BigDecimal subTotal;
    private BigDecimal discountAmount;
    private BigDecimal shippingFee;
    private BigDecimal grandTotal;
}
