package com.poly.BE_main.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "PRODUCT", schema = "dbo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "PRODUCT_NAME", length = 255)
    private String productName;

    @Column(name = "BRAND_ID")
    private Integer brandId;

    @ManyToOne
    @JoinColumn(name = "BRAND_ID", insertable = false, updatable = false)
    private Brand brand;

    @Column(name = "CATEGORY_ID")
    private Integer categoryId;

    @Column(name = "SOLE_ID")
    private Integer soleId;

    @Column(name = "DESCRIPTION", columnDefinition = "TEXT")
    private String description;

    @Column(name = "CREATED_BY", length = 255)
    private String createdBy;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;

    @Column(name = "IS_ACTIVE")
    private boolean isActive;

}