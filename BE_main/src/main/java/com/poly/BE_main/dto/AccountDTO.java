package com.poly.BE_main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountDTO {
    private Integer id;
    private String username;
    private String password;
    private Boolean isActive;
    private Integer roleId;
    private LocalDateTime createdDate;
}
