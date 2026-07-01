package com.poly.BE_main.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "FAVORITE", schema = "dbo")
public class Favorite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "CUSTOMER_ID")
    private Integer customerId;

    @Column(name = "PRODUCT_ID")
    private Integer productId;

    @Column(name = "CREATE_AT")
    private LocalDateTime createAt;

}
