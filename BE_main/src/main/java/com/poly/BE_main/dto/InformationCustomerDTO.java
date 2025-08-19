package com.poly.BE_main.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InformationCustomerDTO {
    private String fullName;
    private String gender;
    private String email;
    private String numberPhone;
    private LocalDate birthOfDate;
}
