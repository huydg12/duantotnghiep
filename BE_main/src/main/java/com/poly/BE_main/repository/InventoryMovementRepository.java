package com.poly.BE_main.repository;

import com.poly.BE_main.model.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Integer> {
    List<InventoryMovement> findByProductDetailIdOrderByCreatedAtDesc(Integer productDetailId);
}
