package com.poly.BE_main.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "CUSTOMER", schema = "dbo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "FULL_NAME", length = 255)
    private String fullName;

    @Column(name = "GENDER", length = 45)
    private String gender;

    @Column(name = "EMAIL", length = 255)
    private String email;

    @Column(name = "NUMBER_PHONE", length = 45)
    private String numberPhone;

    @Column(name = "BIRTH_OF_DATE")
    private LocalDate birthOfDate;

    @Column(name = "STATUS")
    private Integer status;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @OneToOne
    @JoinColumn(name = "ACCOUNT_ID", nullable = true)
    @JsonManagedReference
    private Account account;
}
