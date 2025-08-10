package com.poly.BE_main.model;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "BILL_DETAIL", schema = "dbo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(name = "ID")
    private int id;

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

    @Column(name = "COLOR")
    private String color;

    @Column(name = "SIZE")
    private String size;

    @Column(name = "PRODUCT_NAME")
    private String productName;

    @ManyToOne
    @JoinColumn(name = "BILL_ID")
    @JsonBackReference
    private Bill bill;

    @ManyToOne
    @JoinColumn(name = "PRODUCT_DETAIL_ID", insertable = false, updatable = false)
    private ProductDetail productDetail;
}