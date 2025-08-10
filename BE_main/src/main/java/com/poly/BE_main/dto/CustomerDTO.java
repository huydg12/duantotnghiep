package com.poly.BE_main.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDTO {
    private Integer id;
    private String fullName;
    private String gender;
    private String email;
    private String numberPhone;
    private LocalDate birthOfDate;
    private Integer status;
    private LocalDateTime createdDate;

    private Integer accountId;
}
