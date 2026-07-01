package com.poly.BE_main.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "INVENTORY", schema = "dbo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private int id;

    @Column(name = "PRODUCT_DETAIL_ID")
    private int productDetailId;

    @Column(name = "QUANTITY")
    private int quantity;

    @Column(name = "MODIFIED_DATE")
    private LocalDateTime modifiedDate;

}