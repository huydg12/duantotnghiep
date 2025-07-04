package com.poly.BE_main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDTO {
    private String fullname;
    private String phone;
    private String email;
    private String username;
    private String password;

}
