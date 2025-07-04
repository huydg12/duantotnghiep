package com.poly.BE_main.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="BILL_DETAIL")
public class BillDetail {
    @Id
    @GeneratedValue (strategy = GenerationType.IDENTITY)

    @Column(name = "ID")
    private int id;

    @Column(name = "BILL_ID")
    private int billID;

    @Column(name = "PRODUCT_DETAIL_ID")
    private int productDetailId;

    @Column(name = "QUANTITY")
    private int quantity;

    @Column(name = "PRICE")
    private BigDecimal price;

    @Column(name = "STATUS")
    private int status;

    @Column(name = "PRODUCT_IMAGE")
    private String productImage;

    @Column(name = "SIZE")
    private String size;

    @Column(name = "PRODUCT_NAME")
    private String productName;

    public BillDetail() {
    }

    public BillDetail(int id, int billID, int productDetailId, int quantity, BigDecimal price, int status,
            String productImage, String size, String productName) {
        this.id = id;
        this.billID = billID;
        this.productDetailId = productDetailId;
        this.quantity = quantity;
        this.price = price;
        this.status = status;
        this.productImage = productImage;
        this.size = size;
        this.productName = productName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBillID() {
        return billID;
    }

    public void setBillID(int billID) {
        this.billID = billID;
    }

    public int getProductDetailId() {
        return productDetailId;
    }

    public void setProductDetailId(int productDetailId) {
        this.productDetailId = productDetailId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    
}