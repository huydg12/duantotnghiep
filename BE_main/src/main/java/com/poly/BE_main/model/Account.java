package com.poly.BE_main.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ACCOUNT", schema = "dbo", uniqueConstraints = { @UniqueConstraint(columnNames = "USERNAME") })
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private int id;

    @Column(name = "USERNAME", length = 255)
    private String username;

    @Column(name = "PASSWORD", length = 255)
    private String password;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @PrePersist
    void onCrete() {
        this.createdDate = LocalDateTime.now();
    }

    @Column(name = "IS_ACTIVE")
    private Boolean isActive;

    @Column(name = "ROLE_ID")
    private int roleId;

    @OneToOne(mappedBy = "account")
    @JsonBackReference(value = "customer_account")
    private Customer customer;

    @OneToOne(mappedBy = "account")
    @JsonBackReference(value = "employee_account")
    private Employee employee;

}
