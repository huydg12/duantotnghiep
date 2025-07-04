package com.poly.BE_main.dto;

import com.poly.BE_main.model.Account;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO {
    private int id;
    private String username;
    private int roleId;
    private String email;

    public AccountDTO(Account account) {
        this.id = account.getId();
        this.username = account.getUsername();
        this.roleId = account.getRoleId();
        this.email = account.getEmail();
    }

}
