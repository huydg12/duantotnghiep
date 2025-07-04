package com.poly.BE_main.model;

import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "IMPORT_RECEIPT_DETAIL")
public class ImportReceiptDetail {
    
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    @Column(name="ID")
    private int id;

    @Column(name = "IMPORT_RECEIPT_ID")
    private Integer importReceiptId;

    @Column(name = "PRODUCT_DETAIL_ID")
    private Integer productDetailId;

    @Column(name = "QUANTITY")
    private int quantity;

    @Column(name = "UNIT_PRICE")
    private BigDecimal unitPrice;

    @Column(name = "TOTAL_PRICE")
    private BigDecimal totalPrice;

    @Column(name = "STATUS")
    private int status;

    public ImportReceiptDetail() {
    }

    public ImportReceiptDetail(int id, Integer importReceiptId, Integer productDetailId, int quantity, BigDecimal unitPrice,
            BigDecimal totalPrice, int status) {
        this.id = id;
        this.importReceiptId = importReceiptId;
        this.productDetailId = productDetailId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getImportReceiptId() {
        return importReceiptId;
    }

    public void setImportReceiptId(Integer importReceiptId) {
        this.importReceiptId = importReceiptId;
    }

    public Integer getProductDetailId() {
        return productDetailId;
    }

    public void setProductDetailId(Integer productDetailId) {
        this.productDetailId = productDetailId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

}
