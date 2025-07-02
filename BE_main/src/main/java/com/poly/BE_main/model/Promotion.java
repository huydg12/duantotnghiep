package com.poly.BE_main.model;

import java.math.BigDecimal;
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
}