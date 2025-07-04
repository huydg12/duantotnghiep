package com.poly.BE_main.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
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

    @OneToOne(mappedBy = "account")
    private Customer customer;
    
}
