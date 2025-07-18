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

    @Column(name = "NUMBER_PHONE")
    private String numberPhone;

    @Column(name = "FULL_NAME")
    private String fullName;

    @Column(name = "CUSTOMER_ID")
    private Integer customerId;

    @Column(name = "IS_DEFAULT")
    private boolean isDefault;

    @Column(name = "DETAIL_ADDRESS")
    private String detailAddress;

    @Column(name = "WARD_NAME")
    private String wardName;

    @Column(name = "DISTRICT_NAME")
    private String districtName;

    @Column(name = "CITY_NAME")
    private String cityName;
}