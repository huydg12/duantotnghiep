package com.poly.BE_main.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "IMAGE")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Image {
    @Id

    @GeneratedValue(strategy = GenerationType.IDENTITY)

    @Column(name = "ID")
    private int Id;

    @Column(name = "PRODUCT_DETAIL_ID")
    private int productDetailId;

    @Column(name = "URl")
    private String url;

    @Column(name = "IS_MAIN")
    private boolean isMain;
}
