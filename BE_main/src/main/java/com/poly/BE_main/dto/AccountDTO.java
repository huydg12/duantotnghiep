package com.poly.BE_main.dto;

import com.poly.BE_main.model.Account;

public class AccountDTO {
    private int id;
    private String username;
    private int roieId;
    private String email;

    public AccountDTO() {
    }

    public AccountDTO(Account account) {
        this.id = account.getId();
        this.username = account.getUsername();
        this.roieId = account.getRoleId();
        this.email = account.getEmail();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getRoieId() {
        return roieId;
    }

    public void setRoieId(int roieId) {
        this.roieId = roieId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    

    
}
