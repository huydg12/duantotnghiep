package com.poly.BE_main.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "CUSTOMER", schema = "dbo")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer Id;

    @Column(name = "ACCOUNT_ID")
    private Integer AccountId;

    @Column(name = "FULL_NAME", length = 255)
    private String FullName;

    @Column(name = "GENDER", length = 45)
    private String Gender;

    @Column(name = "EMAIL", length = 255)
    private String Email;

    @Column(name = "NUMBER_PHONE", length = 45)
    private String NumberPhone;

    @Column(name = "BIRTH_OF_DATE")
    private LocalDate BirthOfDate;

    @Column(name = "STATUS")
    private Integer Status;

    @Column(name = "CREATED_DATE")
    private LocalDateTime CreatedDate;


}
