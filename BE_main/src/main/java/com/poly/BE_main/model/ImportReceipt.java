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
@Table(name = "IMPORT_RECEIPT")
public class ImportReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(name = "ID")
    private int id;

    @Column(name = "IMPORT_RECEIPT_CODE")
    private String importReceiptCode;

    
    @Column(name = "EMPLOYEE_ID")
    private Integer  employeeId;

    @Column(name = "IMPORT_DATE")
    private LocalDateTime importDate;

    @Column(name = "TOTAL_AMOUNT")
    private BigDecimal totalAmount;

    @Column(name = "NOTE")
    private String note;

    @Column(name = "STATUS")
    private int status;

    @Column(name = "CREATED_DATE")
    private LocalDateTime createdDate;



    

}