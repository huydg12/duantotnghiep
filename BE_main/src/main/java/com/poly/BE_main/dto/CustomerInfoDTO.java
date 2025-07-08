package com.poly.BE_main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerInfoDTO {
    private String fullName;
    private String numberPhone;
}
