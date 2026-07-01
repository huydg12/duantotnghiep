package com.poly.BE_main.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillDetailDTO {
    private Integer billId;
    @NotNull private Integer productDetailId;
    @NotNull @Positive private Integer quantity;
    private BigDecimal price;
    private String productName;
    private String color;
    private String size;
}
