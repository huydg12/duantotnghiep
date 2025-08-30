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
    private boolean gender;
    private String email;
    private String numberPhone;
    private LocalDate birthOfDate;
    private boolean isActive;
    private String createdBy;
}
