package com.poly.BE_main.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceItemCustomerDTO {
    int productDetailId;
    String name;
    String color;
    String size;
    int quantity;
    BigDecimal price;
    String image;

}
