package com.poly.BE_main.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "CART_DETAIL", schema = "dbo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private int id;

    @Column(name = "CART_ID")
    private int cartId;

    @Column(name = "PRODUCT_DETAIL_ID")
    private int productDetailId;

    @Column(name = "QUANTITY")
    private int quantity;

    @Column(name = "MODIFIED_DATE")
    private LocalDateTime modifiedDate = LocalDateTime.now();

}