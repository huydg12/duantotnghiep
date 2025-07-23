package com.poly.BE_main.dto;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InformationCustomerDTO {
    private String fullName;
    private String email;
    private String numberPhone;
    private Date birthOfDate;
}
