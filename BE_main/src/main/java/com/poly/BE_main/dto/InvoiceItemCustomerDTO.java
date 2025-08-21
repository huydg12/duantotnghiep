package com.poly.BE_main.dto;

import java.math.BigDecimal;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class InvoiceItemCustomerDTO {
    private int productDetailId;
    private String name;
    private String color;
    private String size;
    private int quantity;
    private BigDecimal price;
    private String image;

    // Constructor đúng với query
    public InvoiceItemCustomerDTO(String productName, String colorName, String sizeEu,
            Integer quantity, BigDecimal price, String imageUrl) {
        this.name = productName;
        this.color = colorName;
        this.size = sizeEu;
        this.quantity = quantity;
        this.price = price;
        this.image = imageUrl;
    }
}
