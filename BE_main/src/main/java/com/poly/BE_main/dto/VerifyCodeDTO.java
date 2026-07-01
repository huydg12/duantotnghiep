package com.poly.BE_main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VerifyCodeDTO {
    @NotBlank @Email private String email;
    @NotBlank @Pattern(regexp = "^[0-9]{6}$") private String otpCode;
}
