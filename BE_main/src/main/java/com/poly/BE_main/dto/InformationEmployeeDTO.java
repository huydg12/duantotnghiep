package com.poly.BE_main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InformationEmployeeDTO {
    private String fullName;
    private boolean gender;
    private String email;
    private String numberPhone;
    private LocalDate birthOfDate;
}
