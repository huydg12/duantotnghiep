package com.poly.BE_main.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "PRODUCT", schema = "dbo")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer Id;

    @Column(name = "PRODUCT_NAME", length = 255)
    private String ProductName;

    @Column(name = "BRAND_ID")
    private Integer BrandId;

    @Column(name = "STYLE_ID")
    private Integer StyleId;

    @Column(name = "SOLE_ID")
    private Integer SoleId;

    @Column(name = "DESCRIPTION", columnDefinition = "TEXT")
    private String Description;

    @Column(name = "CREATED_BY", length = 255)
    private String CreatedBy;

    @Column(name = "CREATED_DATE")
    private LocalDateTime CreatedDate;

    @Column(name = "STATUS")
    private Integer Status;

    

    public Product() {
    }

    
    public Product(Integer id, String productName, Integer brandId, Integer styleId, Integer soleId, String description,
            String createdBy, LocalDateTime createdDate, Integer status) {
        Id = id;
        ProductName = productName;
        BrandId = brandId;
        StyleId = styleId;
        SoleId = soleId;
        Description = description;
        CreatedBy = createdBy;
        CreatedDate = createdDate;
        Status = status;
    }

    public Integer getId() {
        return Id;
    }

    public void setId(Integer id) {
        Id = id;
    }

    public String getProductName() {
        return ProductName;
    }

    public void setProductName(String productName) {
        ProductName = productName;
    }

    public Integer getBrandId() {
        return BrandId;
    }

    public void setBrandId(Integer brandId) {
        BrandId = brandId;
    }

    public Integer getStyleId() {
        return StyleId;
    }

    public void setStyleId(Integer styleId) {
        StyleId = styleId;
    }

    public Integer getSoleId() {
        return SoleId;
    }

    public void setSoleId(Integer soleId) {
        SoleId = soleId;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getCreatedBy() {
        return CreatedBy;
    }

    public void setCreatedBy(String createdBy) {
        CreatedBy = createdBy;
    }

    public LocalDateTime getCreatedDate() {
        return CreatedDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        CreatedDate = createdDate;
    }

    public Integer getStatus() {
        return Status;
    }

    public void setStatus(Integer status) {
        Status = status;
    }

    
}