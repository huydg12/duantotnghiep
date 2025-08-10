package com.poly.BE_main.dto;

import com.poly.BE_main.model.Account;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthDTO {
    private int id;
    private String username;
    private int roleId;
    private String email;
    private Integer customerId;
    private Integer employeeId;

    public AuthDTO(Account account) {
        this.id = account.getId();
        this.username = account.getUsername();
        this.roleId = account.getRoleId();
        if (account.getCustomer() != null) {
            this.email = account.getCustomer().getEmail();
        } else if (account.getEmployee() != null) {
            this.email = account.getEmployee().getEmail();
        } else {
            this.email = null;
        }
        this.customerId = account.getCustomer() != null ? account.getCustomer().getId() : null; // Lấy customerId từ Customer
                                                                                                
        this.employeeId = account.getEmployee() != null ? account.getEmployee().getId() : null;
    }

}
