package com.poly.BE_main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductReceiptDTO {
    private Integer productDetailId;
    private String productName;
    private String size;
    private String color;
}
