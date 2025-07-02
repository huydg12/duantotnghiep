package com.poly.BE_main.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "PROMOTION", schema = "dbo")
public class Promotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer Id;

    @Column(name = "PROMOTION_CODE", length = 50)
    private String PromotionCode;

    @Column(name = "NAME", length = 255)
    private String Name;

    @Column(name = "TYPE")
    private Integer Type;

    @Column(name = "VALUE", precision = 10, scale = 2)
    private BigDecimal Value;

    @Column(name = "START_DATE")
    private LocalDateTime StartDate;

    @Column(name = "END_DATE")
    private LocalDateTime EndDate;

    @Column(name = "STATUS")
    private Integer Status;

    @Column(name = "NOTE")
    private String Note;

    @Column(name = "CREATED_DATE")
    private LocalDateTime CreatedDate;

    @Column(name = "MODIFIED_DATE")
    private LocalDateTime ModifiedDate;

    public Promotion(){

    }

    public Promotion(Integer id, String promotionCode, String name, Integer type, BigDecimal value,
                     LocalDateTime startDate, LocalDateTime endDate, Integer status,
                     String note, LocalDateTime createdDate, LocalDateTime modifiedDate) {
        Id = id;
        PromotionCode = promotionCode;
        Name = name;
        Type = type;
        Value = value;
        StartDate = startDate;
        EndDate = endDate;
        Status = status;
        Note = note;
        CreatedDate = createdDate;
        ModifiedDate = modifiedDate;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPromotionCode() {
        return promotionCode;
    }

    public void setPromotionCode(String promotionCode) {
        this.promotionCode = promotionCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getModifiedDate() {
        return modifiedDate;
    }

    public void setModifiedDate(LocalDateTime modifiedDate) {
        this.modifiedDate = modifiedDate;
    }
}