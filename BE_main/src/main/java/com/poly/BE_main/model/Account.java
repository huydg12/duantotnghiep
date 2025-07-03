package com.poly.BE_main.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "ACCOUNT", schema = "dbo", uniqueConstraints = { @UniqueConstraint(columnNames = "USERNAME") })
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private int Id;

    @Column(name = "USERNAME", length = 255)
    private String username;

    @Column(name = "PASSWORD", length = 255)
    private String password;

    @Column(name = "EMAIL", length = 255)
    private String email;

    @Column(name = "NUMBER_PHONE", length = 45)
    private String phone;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createDate;

    @Column(name = "IS_ACTIVE")
    private boolean isActive;

    @Column(name = "ROLE_ID")
    private int roleId;

    public Account() {
    }

    public Account(int id, String username, String password, String email, String phone, LocalDateTime createDate,
            boolean isActive, int roleId) {
        Id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.createDate = createDate;
        this.isActive = isActive;
        this.roleId = roleId;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

}
