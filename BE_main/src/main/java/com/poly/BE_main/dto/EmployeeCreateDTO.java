package com.poly.BE_main.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeCreateDTO {
    private String username;
    private String password;
    private Integer roleId;
    private String fullName;
    private String gender;
    private String email;
    private String numberPhone;
    private LocalDate birthOfDate;
    private Boolean isActive;
    private String createdBy;
}
