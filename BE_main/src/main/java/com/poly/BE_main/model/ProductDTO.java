package com.poly.BE_main.model;

import jakarta.persistence.Entity;

@Entity
public class ProductDTO {
    private Long id;
    private String name;
    private String brand;
    private Double price;
    private String image;


}
