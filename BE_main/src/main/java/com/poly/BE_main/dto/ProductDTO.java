package com.poly.BE_main.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class ProductDTO {
    private Integer productId; // hoặc int nếu không cần nullable
    private String productName;
    private String brandName;
    private BigDecimal price;
    private String image1; // ảnh đầu tiên (chính)
    private String image2; // ảnh hover (thứ hai)

}
