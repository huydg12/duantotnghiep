package com.poly.BE_main.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvoiceItemCustomerDTO {
    String name;
    String color;
    String size;
    int quantity;
    BigDecimal price;
    String image;

    
}
