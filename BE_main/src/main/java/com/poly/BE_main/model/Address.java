package com.poly.BE_main.model;

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

@Entity
@Table(name = "ADDRESS", schema = "dbo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private int Id;

    @Column(name = "FULL_ADDRESS")
    private String fullAddress;

    @Column(name = "ADDRESS")
    private String address;

    @Column(name = "CREATED_BY")
    private String createdBy;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "MODIFIED_BY")
    private String modifiedBy;

    @Column(name = "MODIFIED_DATE")
    private LocalDateTime modifiedDate;

    @Column(name = "NUMBER_PHONE")
    private String numberPhone;

    @Column(name = "FULL_NAME")
    private String fullName;

    @Column(name = "CUSTOMER_ID")
    private Integer customerId;

    @Column(name = "IS_DEFAULT")
    private boolean isDefault;

    @Column(name = "ADDRESS_TYPE")
    private String addressType;

    @Column(name = "STREET_NAME")
    private String streetName;

    @Column(name = "WARD_NAME")
    private String wardName;

    @Column(name = "CITY_NAME")
    private String cityName;
}