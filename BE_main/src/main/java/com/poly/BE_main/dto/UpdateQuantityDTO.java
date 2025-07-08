package com.poly.BE_main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateQuantityDTO {
        private Integer cartId;
    private Integer productDetailId;
    private Integer quantity;
}
