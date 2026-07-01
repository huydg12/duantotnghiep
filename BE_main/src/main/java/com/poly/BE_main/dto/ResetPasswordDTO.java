package com.poly.BE_main.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordDTO {
    @NotBlank @Email private String email;
    @NotBlank @Pattern(regexp = "^[0-9]{6}$") private String otpCode;
    @NotBlank @Size(min = 8, max = 72) private String newPassword;
}
