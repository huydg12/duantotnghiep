package com.poly.BE_main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeDTO {
    private Integer id;
    private String fullName;
    private boolean gender;
    private String email;
    private String numberPhone;
    private LocalDate birthOfDate;
    private boolean isActive;
    private String createdBy;
    private LocalDateTime createdDate;
    private Integer accountId;
    private String accountUsername;
}
