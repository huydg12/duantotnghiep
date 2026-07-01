package com.poly.BE_main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryDTO {
    private int id;
    private String productName;
    private String color;
    private String size;
    private int quantity;
    private LocalDateTime modifiedDate;
    private BigDecimal unitPrice;

}
