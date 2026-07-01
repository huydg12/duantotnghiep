package com.poly.BE_main.model;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.*;
@Entity @Table(name="INVENTORY_MOVEMENT", schema="dbo")
@Data @NoArgsConstructor @AllArgsConstructor
public class InventoryMovement {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="ID") private Integer id;
    @Column(name="PRODUCT_DETAIL_ID", nullable=false) private Integer productDetailId;
    @Column(name="QUANTITY_CHANGE", nullable=false) private Integer quantityChange;
    @Column(name="BALANCE_AFTER", nullable=false) private Integer balanceAfter;
    @Column(name="MOVEMENT_TYPE", nullable=false, length=30) private String movementType;
    @Column(name="REFERENCE_TYPE", length=30) private String referenceType;
    @Column(name="REFERENCE_ID") private Integer referenceId;
    @Column(name="CREATED_BY", nullable=false, length=255) private String createdBy;
    @Column(name="CREATED_AT", nullable=false) private LocalDateTime createdAt;
}
