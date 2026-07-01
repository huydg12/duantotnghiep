package com.poly.BE_main.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterDTO {
    @NotBlank
    @Size(max = 255)
    private String fullname;
    @NotBlank
    @Pattern(regexp = "^[0-9]{9,11}$")
    private String phone;
    @NotBlank
    @Email
    @Size(max = 255)
    private String email;
    @NotBlank
    @Size(min = 4, max = 255)
    private String username;
    @NotBlank
    @Size(min = 8, max = 72)
    private String password;

}
